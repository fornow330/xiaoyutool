const CACHE = 'xy-toolbox-v2';

self.addEventListener('install', e => { self.skipWaiting(); });

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys()
      .then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

function isDoc(req, url) {
  return req.mode === 'navigate'
    || url.pathname.endsWith('/')
    || url.pathname.endsWith('/index.html');
}

self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== location.origin) return;

  if (isDoc(req, url)) {
    // 主文档：网络优先，3 秒超时回退缓存，保证打开就是新版本
    e.respondWith((async () => {
      const cached = await caches.match(req);
      try {
        const ctl = new AbortController();
        const timer = setTimeout(() => ctl.abort(), 3000);
        const net = await fetch(req, { signal: ctl.signal });
        clearTimeout(timer);
        const copy = net.clone();
        caches.open(CACHE).then(c => c.put(req, copy)).catch(() => {});
        return net;
      } catch (err) {
        return cached || caches.match('./index.html');
      }
    })());
    return;
  }

  // 图标、manifest 等静态资源：缓存优先，后台静默更新
  e.respondWith((async () => {
    const cached = await caches.match(req);
    const net = fetch(req).then(r => {
      const copy = r.clone();
      caches.open(CACHE).then(c => c.put(req, copy)).catch(() => {});
      return r;
    }).catch(() => null);
    if (cached) { e.waitUntil(net); return cached; }
    const r = await net;
    return r || caches.match('./index.html');
  })());
});
