if (window.__dedupeInstalled) { /* 既に実行済み */ } else {
window.__dedupeInstalled = true;
(function () {
  'use strict';

  // ---- 設定 ----
  const HIDE_DUPLICATES = true;   // 同じ商品の2回目以降を隠す
  const HIDE_PR = false;          // 「PR」表示の商品も隠す(true で有効)
  const MAX_PER_SHOP = 0;         // 同じショップの商品を最大何件まで表示(0 = 無制限)
  const MARK_ONLY = false;        // true なら隠さず半透明にするだけ(動作確認用)

  const ITEM_RE = /item\.rakuten\.co\.jp\/([^\/?#]+)\/([^\/?#]+)/;
  const seenItems = new Set();
  const shopCount = new Map();
  const processed = new WeakSet();

  function parseItem(a) {
    let href = a.href || '';
    let m = href.match(ITEM_RE);
    if (!m) {
      // 広告リダイレクトなど、URLがエンコードされて埋め込まれている場合
      try { m = decodeURIComponent(href).match(ITEM_RE); } catch (e) { /* ignore */ }
    }
    return m ? { shop: m[1], item: m[2] } : null;
  }

  // anchor を含み、他の商品を含まない最大の祖先 = 商品カード
  function findCard(a, key) {
    let el = a;
    while (el.parentElement && el.parentElement !== document.body) {
      const p = el.parentElement;
      const keys = new Set();
      for (const l of p.querySelectorAll('a[href]')) {
        const k = parseItem(l);
        if (k) { keys.add(k.shop + '/' + k.item); if (keys.size > 1) break; }
      }
      if (keys.size > 1) break;
      el = p;
    }
    return el;
  }

  function isPR(card) {
    // 「PR」だけを示す小さなラベルを探す
    for (const n of card.querySelectorAll('span,div,p,small,i')) {
      if (n.children.length === 0 && n.textContent.trim() === 'PR') return true;
    }
    return false;
  }

  function hide(card, reason) {
    if (MARK_ONLY) {
      card.style.opacity = '0.25';
      card.title = reason;
    } else {
      card.style.display = 'none';
    }
    card.dataset.rdHidden = reason;
  }

  function scan() {
    const anchors = document.querySelectorAll('a[href]');
    for (const a of anchors) {
      const k = parseItem(a);
      if (!k) continue;
      const card = findCard(a, k);
      if (processed.has(card)) continue;
      processed.add(card);

      const key = k.shop + '/' + k.item;

      if (HIDE_PR && isPR(card)) { hide(card, 'PR'); continue; }

      if (HIDE_DUPLICATES && seenItems.has(key)) { hide(card, '重複'); continue; }
      seenItems.add(key);

      if (MAX_PER_SHOP > 0) {
        const n = (shopCount.get(k.shop) || 0) + 1;
        shopCount.set(k.shop, n);
        if (n > MAX_PER_SHOP) hide(card, 'ショップ上限');
      }
    }
  }

  let timer = null;
  const schedule = () => { clearTimeout(timer); timer = setTimeout(scan, 200); };

  scan();
  new MutationObserver(schedule).observe(document.body, { childList: true, subtree: true });
})();
}
