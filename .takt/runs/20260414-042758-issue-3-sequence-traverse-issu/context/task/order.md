## Issue #3: コレクション連携として sequence / traverse を追加する

# Issue: コレクション連携として `sequence` / `traverse` を追加する

## 背景
- `Option.stream()` がすでにあり、Java Stream との接続は一部考慮されている。
- 一方で、複数の `Option` / `Result` / `Try` をまとめて 1 つの結果へ畳み込むユースケースは現状サポートがない。
- 実務では、複数入力の検証、複数 parse、複数 I/O の逐次評価結果をまとめたい場面が多い。

## 課題
- `List<Option<T>>` や `List<Result<T,E>>` をまとめる処理を利用者が毎回手書きする必要がある。
- コレクションを横断する共通パターンがライブラリ外へ漏れている。
- 最小 API を保ちつつ、実利用での表現力を上げる余地がある。

## 提案
- 以下のようなコレクション連携 API を追加検討する。
  - `Option.sequence(Iterable<Option<T>>): Option<List<T>>`
  - `Result.sequence(Iterable<Result<T,E>>): Result<List<T>, E>`
  - 必要に応じて `Try.sequence(Iterable<Try<T>>): Try<List<T>>`
  - 併せて `traverse` も検討する。
- 意味論は単純に保つ。
  - `Option`: 1 つでも `None` があれば `None`
  - `Result`: 最初の `Failure` で失敗
  - `Try`: 最初の `Failure` で失敗

## スコープ案
- まずは `Iterable` 入力と `List` 出力で十分。
- エラー蓄積型の `Validation` は別 Issue に切り出す。
- Stream 直接対応は後回しでよい。

## 受け入れ条件
- 空コレクション、全成功、途中失敗のケースがテストされている。
- `null` 要素や `null` iterable をどう扱うかが定義されている。
- README に複数入力検証または複数 parse のサンプルがある。

## 補足
- `Validation` を入れない段階でも、`sequence` / `traverse` は十分に実用価値がある。
