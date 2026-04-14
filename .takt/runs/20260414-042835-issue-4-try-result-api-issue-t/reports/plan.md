# タスク計画

## 元の要求
Implement using only the files in `.takt/runs/20260414-042835-issue-4-try-result-api-issue-t/context/task`.
Primary spec: `.takt/runs/20260414-042835-issue-4-try-result-api-issue-t/context/task/order.md`.
Use report files in Report Directory as primary execution history.
Do not rely on previous response or conversation summary.

## 分析結果

### 目的
`Try` / `Result` に失敗からの復旧 API を最小構成で追加し、`Success` / `Failure` 両経路のテストと README の利用例を整備する。

### 分解した要件
| # | 要件 | 種別 | 備考 |
|---|------|------|------|
| 1 | `Try` に `recover(Function<? super Throwable, ? extends T>)` 相当の復旧 API を追加する | 明示 | `order.md` の提案 |
| 2 | `Try` に `recoverWith(Function<? super Throwable, Try<T>>)` 相当の復旧 API を追加する | 明示 | `order.md` の提案 |
| 3 | `Result` に `recover(Function<? super E, ? extends T>)` 相当の復旧 API を追加する | 明示 | `order.md` の提案 |
| 4 | `Result` に `recoverWith(Function<? super E, Result<T, E>>)` または同等 API を追加する | 明示 | `order.md` の提案 |
| 5 | 右バイアス設計を維持し、成功経路はそのまま、失敗経路のみを扱う API に限定する | 明示 | `fold` 追加禁止の根拠でもある |
| 6 | `fold` や包括的な分岐 API を追加しない | 明示 | `order.md` のスコープ案 |
| 7 | `Try.Success` / `Try.Failure` の双方に対するテストを追加する | 明示 | 受け入れ条件 |
| 8 | `Result.Success` / `Result.Failure` の双方に対するテストを追加する | 明示 | 受け入れ条件 |
| 9 | mapper 自体が `null` の場合の挙動を定義しテストする | 明示 | 受け入れ条件 |
| 10 | mapper が `null` を返した場合の挙動を定義しテストする | 明示 | 受け入れ条件 |
| 11 | README に典型的な例外処理の復旧例を追加する | 明示 | 受け入れ条件 |
| 12 | README にバリデーション失敗からの復旧例を追加する | 明示 | 受け入れ条件 |
| 13 | inactive 側では mapper を評価しないことを維持する | 暗黙 | 要件 5 の「成功経路はそのまま」から直接導出 |
| 14 | 既存 API の意味変更や追加の汎用 API 導入は行わない | 暗黙 | 要件 5, 6 の制約から直接導出 |

### 参照資料の調査結果（参照資料がある場合）
参照資料は `context/task/order.md` のみで、外部実装参照はありません。したがって、設計アプローチは現行コードから裏取りしました。

現行実装との差異は以下です。

- `Try` は `map` / `flatMap` / `onSuccess` / `onFailure` / 変換系のみを持ち、復旧 API は未実装
- `Result` は `map` / `mapFailure` / `flatMap` / `onSuccess` / `onFailure` / `tee` / 変換系のみを持ち、失敗から成功へ戻す API は未実装
- README に `recover` / `recoverWith` の説明と例はない
- テストに復旧 API の仕様は存在しない

主要な現行コード根拠:

- `Try` の公開 API: `src/main/java/com/github/rshindo/jfunc/Try.java:113-176`
- `Try.Success` / `Try.Failure` の variant 実装: `src/main/java/com/github/rshindo/jfunc/Try.java:183-329`
- `Result` の公開 API: `src/main/java/com/github/rshindo/jfunc/Result.java:61-125`
- `Result.Success` / `Result.Failure` の variant 実装: `src/main/java/com/github/rshindo/jfunc/Result.java:130-234`
- `TryTest`: `src/test/java/com/github/rshindo/jfunc/TryTest.java`
- `ResultTest`: `src/test/java/com/github/rshindo/jfunc/ResultTest.java`
- README の `Result` / `Try` セクション: `README.md:111-207`

### スコープ
影響範囲は次の 5 ファイルです。

- `src/main/java/com/github/rshindo/jfunc/Try.java`
- `src/main/java/com/github/rshindo/jfunc/Result.java`
- `src/test/java/com/github/rshindo/jfunc/TryTest.java`
- `src/test/java/com/github/rshindo/jfunc/ResultTest.java`
- `README.md`

変更不要なもの:

- `Either` / `Option` / `Tuple` / `Unit` には本件から直接導かれる変更なし
- `fold` 系 API の追加は不要。現状も pattern matching 優先で未提供
- ビルド設定や依存追加は不要

### 検討したアプローチ（設計判断がある場合）
| アプローチ | 採否 | 理由 |
|-----------|------|------|
| `recover` / `recoverWith` を `Try` / `Result` の sealed interface に追加し、各 variant に実装する | 採用 | 現行の `map` / `flatMap` / `mapFailure` と同じ構造で最小変更 |
| `recover` をコンテナ外の値返却 API にする | 不採用 | 既存 API は同じコンテナ型を返す設計で、右バイアスの連鎖を維持しにくい |
| `Result.recoverWith` で failure 型変更まで許す | 不採用 | 指示書は `Result<T, E>` 維持の案を示しており、最小 API から外れる |
| `fold` / `getOrElse` のような包括 API を追加する | 不採用 | `order.md` で明示的にスコープ外 |
| 既存 `map` / `flatMap` / `mapFailure` の null セマンティクスまで同時に修正する | 不採用 | 本件は新 API の追加が主目的であり、既存 API の意味変更はスコープ外 |

### 実装アプローチ
1. `Try` と `Result` の interface に `recover` / `recoverWith` を追加する。
2. `Success` 側は no-op とし、元のインスタンスを返す実装にする。
3. `Failure` 側のみ mapper を評価し、`recover` は `Success` 化、`recoverWith` は mapper の返したコンテナを返す。
4. `null` 方針は現行の active-side 実装パターンに合わせる。
   - active 側で mapper が `null`: `NullPointerException`
   - active 側で `recover` mapper が `null` を返す: `IllegalArgumentException`
   - active 側で `recoverWith` mapper が `null` を返す: `IllegalArgumentException`
   - inactive 側では mapper を評価しない
5. テストを先に追加し、`Success` / `Failure` 双方の復旧・no-op・null ケースを固定する。
6. README に `Result` のバリデーション復旧例と `Try` の例外復旧例を追記する。

### 到達経路・起動条件（利用者向け機能の追加/変更がある場合）
| 項目 | 内容 |
|------|------|
| 利用者が到達する入口 | 入口なし。ライブラリ利用者が `Try` / `Result` の新メソッドを直接呼び出す |
| 更新が必要な呼び出し元・配線 | 配線変更なし。公開 API 追加のみ。更新対象は `Try.java` / `Result.java` / 対応テスト / `README.md` |
| 起動条件 | 既存の `Try` / `Result` インスタンスを保持していること |
| 未対応項目 | なし |

## 実装ガイドライン（設計が必要な場合のみ）
- 既存パターンを踏襲し、interface 宣言と nested record 実装を同一ファイル内で完結させること。
- `Try` の実装参照:
  - `map`: `src/main/java/com/github/rshindo/jfunc/Try.java:193-200`
  - `flatMap`: `src/main/java/com/github/rshindo/jfunc/Try.java:205-208`
  - `Failure` の no-op / 伝播: `src/main/java/com/github/rshindo/jfunc/Try.java:272-329`
- `Result` の実装参照:
  - `map`: `src/main/java/com/github/rshindo/jfunc/Result.java:137-144`
  - `mapFailure`: `src/main/java/com/github/rshindo/jfunc/Result.java:197-204`
  - `flatMap`: `src/main/java/com/github/rshindo/jfunc/Result.java:151-154`, `206-209`
- `recoverWith` の null 戻り値は明示的に弾くこと。ここを素通しにすると受け入れ条件の「挙動定義」が満たせない。
- inactive 側で mapper を評価しないことをテストで固定すること。`Success` 側の `recover(null)` / `recoverWith(null)`、`Result.Success` 側の同ケースを no-op として扱う。
- テスト追加時は以下を最低限含めること。
  - `Try.Success` の `recover` / `recoverWith` は元インスタンス返却
  - `Try.Failure` の `recover` は `Success`
  - `Try.Failure` の `recoverWith` は `Success` / `Failure` の両方に復旧可能
  - `Result.Success` の `recover` / `recoverWith` は元インスタンス返却
  - `Result.Failure` の `recover` は `Success`
  - `Result.Failure` の `recoverWith` は `Success` / `Failure` の両方に復旧可能
  - active-side の `null mapper`
  - active-side の `null return`
- README の追記位置は既存の `Result (ROP)` セクションと `Try` セクションのコード例直下が適切。
- 今回の変更で未使用コードは発生しない見込みのため、削除計画は不要。

## スコープ外（項目がある場合のみ）
| 項目 | 除外理由 |
|------|---------|
| `fold` / `getOrElse` / `orElse` など他の補助 API 追加 | 指示書で最小限の復旧 API に絞ると明記されているため |
| `Either` への同種 API 追加 | 指示書の対象外 |
| 既存 `map` / `flatMap` / `mapFailure` の null セマンティクス変更 | 新 API 追加とは別の仕様変更になるため |
| ビルド設定・依存関係・Java バージョン変更 | 指示書の対象外 |

## 確認事項（あれば）
なし