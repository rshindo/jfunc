# タスク計画

## 元の要求
Implement using only the files in `.takt/runs/20260414-042758-issue-3-sequence-traverse-issu/context/task`.
Primary spec: `.takt/runs/20260414-042758-issue-3-sequence-traverse-issu/context/task/order.md`.
Use report files in Report Directory as primary execution history.
Do not rely on previous response or conversation summary.

## 分析結果

### 目的
`Option` と `Result` にコレクション連携 API として `sequence` / `traverse` を追加し、空コレクション・全成功・途中失敗・`null` 入力の扱いをテストで定義し、README に利用例を追記する。`Try` については spec 上 optional であり、今回の最小スコープでは対象外とする。

### 分解した要件
| # | 要件 | 種別 | 備考 |
|---|------|------|------|
| 1 | `Option.sequence(Iterable<Option<T>>): Option<List<T>>` を追加する | 明示 | `order.md` の提案に明記 |
| 2 | `Result.sequence(Iterable<Result<T,E>>): Result<List<T>, E>` を追加する | 明示 | `order.md` の提案に明記 |
| 3 | `Option` に `traverse` を追加する | 明示 | Issue タイトルと「併せて traverse も検討する」から直接導出 |
| 4 | `Result` に `traverse` を追加する | 明示 | Issue タイトルと「併せて traverse も検討する」から直接導出 |
| 5 | `Option.sequence` は 1 つでも `None` があれば `None` を返す | 明示 | 意味論に明記 |
| 6 | `Result.sequence` は最初の `Failure` で失敗する | 明示 | 意味論に明記 |
| 7 | 入力は `Iterable`、出力は `List` に限定する | 明示 | スコープ案に明記 |
| 8 | 空コレクションの挙動をテストで定義する | 明示 | 受け入れ条件に明記 |
| 9 | 全成功の挙動をテストで定義する | 明示 | 受け入れ条件に明記 |
| 10 | 途中失敗の挙動をテストで定義する | 明示 | 受け入れ条件に明記 |
| 11 | `null` iterable の扱いを定義する | 明示 | 受け入れ条件に明記 |
| 12 | `null` 要素の扱いを定義する | 明示 | 受け入れ条件に明記 |
| 13 | README に複数入力検証または複数 parse のサンプルを追加する | 明示 | 受け入れ条件に明記 |
| 14 | `Validation` は追加しない | 明示 | スコープ外として明記 |
| 15 | Stream 直接対応は追加しない | 明示 | スコープ外として明記 |
| 16 | 新 API は既存方針どおり static メソッドとして公開し、default メソッドは増やさない | 暗黙 | AGENTS.md の API 方針と既存実装から直接導出 |
| 17 | `Try.sequence` / `Try.traverse` は今回は追加しない | 明示 | `order.md` が「必要に応じて」としており必須ではない |

### 参照資料の調査結果（参照資料がある場合）
参照資料は `order.md` のみだった。内容は、`Option` / `Result` / `Try` の複数値を 1 つの結果へ畳み込む用途に対して `sequence` / `traverse` を追加するという issue 定義であり、最初のスコープを `Iterable` 入力・`List` 出力に絞り、`Validation` と Stream 直接対応を見送る方針だった。

現行実装との差分は以下のとおり。
- `Option` には static API として `some` / `none` / `ofNullable` / `fromOptional` はあるが、`sequence` / `traverse` は未実装。
- `Result` には static API として `success` / `failure` はあるが、`sequence` / `traverse` は未実装。
- README には `Option` / `Result` / `Try` の単体利用例はあるが、複数値を畳み込むサンプルはない。
- Report Directory には既存レポートファイルが存在せず、参照できる実行履歴はなかった。

### スコープ
- 変更対象
  - `src/main/java/com/github/rshindo/jfunc/Option.java`
  - `src/main/java/com/github/rshindo/jfunc/Result.java`
  - `src/test/java/com/github/rshindo/jfunc/OptionTest.java`
  - `src/test/java/com/github/rshindo/jfunc/ResultTest.java`
  - `README.md`
- 影響内容
  - 公開 API 追加
  - ユニットテスト追加
  - README の使用例追加
- 配線変更
  - なし。ライブラリの static メソッド追加のみ

### 検討したアプローチ（設計判断がある場合）
| アプローチ | 採否 | 理由 |
|-----------|------|------|
| `Option` / `Result` に static `sequence` / `traverse` を追加する | 採用 | 既存の static factory 配置と整合し、default メソッドを増やさずに済む |
| `traverse` を `sequence(map(...))` の糖衣として実装する | 不採用 | 中間コレクションが増えやすく、1 パス short-circuit の設計を崩す |
| `traverse` も `sequence` も単一パスで逐次 short-circuit 実装する | 採用 | issue の意味論と最小 API に最も素直 |
| `Try.sequence` / `Try.traverse` も同時に追加する | 不採用 | spec で optional。`Try.java` は既に 331 行で、任意機能の追加でさらに肥大化させるのは今回の最小スコープに反する |
| Stream overload を追加する | 不採用 | spec が後回しと明記している |
| `Validation` 的なエラー蓄積を入れる | 不採用 | spec が別 Issue として除外している |

### 実装アプローチ
`Option` と `Result` に static `sequence` / `traverse` を追加する。どちらも `Iterable` を 1 回だけ走査し、成功値を `ArrayList` に蓄積しつつ、`Option` は最初の `None`、`Result` は最初の `Failure` で即座に返す。最後まで成功した場合のみ `List` を返す。

`null` 契約は新 API で明示的に定義する。既存の public factory 系 API が `null` 入力を `IllegalArgumentException` で拒否しているため、それに合わせて以下で統一する。
- `null` iterable: `IllegalArgumentException`
- `null` 要素: `IllegalArgumentException`
- `null` mapper: `IllegalArgumentException`
- `traverse` の mapper 戻り値が `null`: `IllegalArgumentException`

テストは `OptionTest` と `ResultTest` に追加する。空コレクション、全成功、途中失敗、`null` iterable、`null` 要素、`null` mapper、`traverse` の失敗経路をそれぞれ独立テストに分ける。README は `Result.traverse` を使った複数 parse か `Option.sequence` を使った複数入力検証のどちらか 1 例を追加する。

### 到達経路・起動条件（利用者向け機能の追加/変更がある場合）
| 項目 | 内容 |
|------|------|
| 利用者が到達する入口 | `Option.sequence(...)` / `Option.traverse(...)` / `Result.sequence(...)` / `Result.traverse(...)` の static 呼び出し |
| 更新が必要な呼び出し元・配線 | ライブラリ内部の配線変更はなし。README のサンプル更新のみ |
| 起動条件 | Java 21 上でライブラリ API を直接呼び出すこと |
| 未対応項目 | `Try.sequence` / `Try.traverse`、Stream 直接対応、`Validation` は未対応 |

## 実装ガイドライン（設計が必要な場合のみ）
- static メソッドの配置は既存の factory 群に合わせる。`Option` は `some` / `none` / `ofNullable` / `fromOptional` の近傍、`Result` は `success` / `failure` の近傍に追加する。
- default メソッドは増やさない。AGENTS.md の方針どおり、挙動追加は static API で行う。
- `Option` の short-circuit は既存の `Some.map` / `None.map`、`Some.flatMap` / `None.flatMap` の思想に合わせる。
- `Result` の short-circuit は既存の `Success.flatMap` / `Failure.flatMap` の思想に合わせる。
- `traverse` は中間 `List<Option<T>>` や `List<Result<T,E>>` を構築せず、1 パスで処理する。
- 公開メソッドの Javadoc は日本語で簡潔に書く。既存ファイルの英語 Javadoc 全体を書き換える必要はないが、新規追加分は project ルールに従う。
- 追加テストは `OptionTest` / `ResultTest` に集約し、`Some` と `None`、`Success` と `Failure` の両経路を必ず含める。
- README の例は複数値をまとめる実用シナリオに限定し、不要な API 拡張を示唆しない。
- 実装後の後続ステップでは `mvn test` を必ず実行して確認する。

## スコープ外（項目がある場合のみ）
| 項目 | 除外理由 |
|------|---------|
| `Try.sequence` / `Try.traverse` | spec で optional。今回の必須受け入れ条件は `Option` / `Result` で満たせる |
| `Validation` | spec が別 Issue として明示的に除外 |
| Stream 直接対応 | spec が後回しと明示 |
| 既存 Javadoc の全面統一 | 今回の要求に含まれない |

## 確認事項（あれば）
なし