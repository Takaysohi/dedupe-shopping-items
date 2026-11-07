# 重複商品スッキリ(楽天市場用・非公式)

楽天市場の検索結果をスクロールすると、同じ商品が何度も出てきます。このユーザースクリプトは、同じ商品の2回目以降を自動で非表示にします。楽天とは関係のない、個人が作った非公式のツールです。

## 入れ方(Android)

**[ボタンで進める案内ページ](https://takaysohi.github.io/dedupe-shopping-items/)** を開くと、手順をボタンで進められます。手で進めるときは次のとおりです。

1. **Firefox** を入れる: [Play ストア](https://play.google.com/store/apps/details?id=org.mozilla.firefox)
2. Firefox で **Tampermonkey** を追加する: [追加ページ](https://addons.mozilla.org/ja/android/addon/tampermonkey/)
3. Firefox でこのページを開き、下のリンクをタップして「インストール」を押す
   - **[スクリプトをインストール](https://raw.githubusercontent.com/Takaysohi/dedupe-shopping-items/main/dedupe-shopping-items.user.js)**
4. Firefox で楽天市場を開く

## 設定

Tampermonkey でスクリプトを開くと、先頭の4行で設定を変えられます。

| 設定 | 初期値 | 内容 |
|---|---|---|
| HIDE_DUPLICATES | true | 同じ商品の2回目以降を隠す |
| HIDE_PR | false | 「PR」表示の商品も隠す |
| MAX_PER_SHOP | 0 | 同じショップの表示上限(0 は無制限) |
| MARK_ONLY | false | 隠さず薄く表示する(動作確認用) |

## 注意

- 楽天市場の画面が変わると、動かなくなることがあります。
- 表示を隠すだけで、楽天のデータには何も送りません。
- 動かないときや、関係ない商品が消えるときは、Issues で知らせてください。

## ライセンス

MIT
