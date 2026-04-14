## Issue #2: Option / Either / Result / Try 間の変換 API を一貫化する

# Issue: `Option` / `Either` / `Result` / `Try` 間の変換 API を一貫化する

## 背景
- 現在の `jfunc` には `Option` / `Either` / `Result` / `Try` があり、型ごとに一部の変換 API は提供されている。
- ただし、どの型からどの型へどう変換できるかが完全には揃っておらず、利用者が都度 API を調べる必要がある。
- このライブラリは「tiny, typed functional utilities」を掲げているため、型間の接続は最小 API の範囲で揃っている方が価値が高い。

## 課題
- 変換 API の命名と粒度が型ごとに揃っていない。
- 失敗情報を保持する変換と、失敗情報を捨てる変換の境界が見えづらい。
- `Either` / `Result` / `Try` をパイプライン中で組み合わせる際に、余計な手書き変換が発生する。

## 提案
- 各型の変換 API を棚卸しし、以下の観点で整理する。
  - 保持型変換: 失敗情報や例外情報を保持したまま別の型へ移す。
  - 縮約型変換: `Option` のように情報を落として存在/不在へ変換する。
- 少なくとも以下を検討する。
  - `Either` <-> `Result`
  - `Try` -> `Either`
  - `Try` -> `Result`
  - `Either` / `Result` / `Try` -> `Option`
- 命名は既存 API に寄せ、パターンマッチ優先の方針を崩さない。

## スコープ案
- 既存公開 API の意味変更は避ける。
- `fold` のような汎用ヘルパーは追加しない。
- 必要なら README の変換マトリクスも更新する。

## 受け入れ条件
- 型間変換の提供方針が README または Javadoc で明示されている。
- 主要な型変換に対して、`Some`/`None`、`Left`/`Right`、`Success`/`Failure` の経路がテストされている。
- `null` ポリシーが既存設計と矛盾しない。

## 補足
- とくに `Either` と `Result` の相互変換は利用価値が高いので優先実装候補。
