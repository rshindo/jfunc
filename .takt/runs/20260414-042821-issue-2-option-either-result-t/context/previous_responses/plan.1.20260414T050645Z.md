依頼の範囲に合わせて、まず `.takt/.../context/task` の参照資料を読み、仕様と現行実装の差分を特定します。続いて `Option` / `Either` の実装と既存テストを確認し、変更要否と影響範囲を確定します。
参照資料の主仕様は確認できました。次に task ディレクトリ全体と、実装側の `Option` / `Either` / `Result` / `Try` および対応テストを読み、既存の変換 API の有無を具体的に洗います。
実装の輪郭は見えました。`Try` には `toEither` / `toResult` が既にあり、未整備なのは主に `Either` と `Result` の相互変換と、そのテスト・ドキュメントの一貫性です。
既存パターンの参照箇所とテスト追加先を行番号付きで固めます。実装自体はまだ行わず、次工程が迷わない粒度まで落とし込みます。
**調査結果**

参照資料は `.takt/.../context/task/order.md` のみで、追加の参照資料はありませんでした。レポートディレクトリにも既存レポートはなく、差し戻し前提の履歴はありません。

このタスクは小規模ではありません。理由は、公開 API を `Either` と `Result` に追加し、対応テストと変換方針の文書化まで必要だからです。

**要件ごとの変更要否**

- `Either <-> Result`: 変更要。
  根拠: [Either.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Either.java:17) には `toOptionRight` / `toOptionLeft` までしかなく、`toResult()` がないです（[Either.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Either.java:106)）。[Result.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Result.java:27) も `toOptionSuccess` / `toOptionFailure` までで、`toEither()` がありません（[Result.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Result.java:113)）。
- `Try -> Either`: 変更不要。
  根拠: `Try.toEither()` は既に宣言・実装済みです（[Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:164), [Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:243), [Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:318)）。テストもあります（[TryTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/TryTest.java:75)）。
- `Try -> Result`: 変更不要。
  根拠: `Try.toResult()` は既に宣言・実装済みです（[Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:171), [Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:251), [Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:326)）。テストもあります（[TryTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/TryTest.java:75)）。
- `Either` / `Result` / `Try` -> `Option`: 変更不要。
  根拠: `Either` は [Either.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Either.java:106)、`Result` は [Result.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Result.java:113)、`Try` は [Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:150) に既存 API があります。テストもそれぞれ [EitherTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/EitherTest.java:75), [ResultTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/ResultTest.java:106), [TryTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/TryTest.java:64) にあります。
- 変換方針の明示化: 変更要。
  根拠: README は各型ごとの個別説明はありますが、型間変換の方針やマトリクスは未記載です（[README.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/README.md:11), [README.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/README.md:14), [README.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/README.md:18)）。

**設計方針**

追加する公開 API は最小限に留めます。今回必要なのは `Either#toResult()` と `Result#toEither()` のみです。`fold` や汎用変換ヘルパーは追加しません。これは issue の「パターンマッチ優先」「最小 API」方針に一致します。

実装は既存方針どおり interface に抽象メソッドを追加し、挙動は record 側に持たせます。つまり default メソッドは使わず、`Either.Left` / `Either.Right` と `Result.Success` / `Result.Failure` に個別実装します。既存パターンは `Either.toOption*` と `Try.toEither` / `Try.toResult` です（[Either.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Either.java:106), [Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:164)）。

**影響範囲**

- 実装: [Either.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Either.java:17), [Result.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Result.java:27)
- テスト: [EitherTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/EitherTest.java:10), [ResultTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/ResultTest.java:10)
- ドキュメント: [README.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/README.md:5)

利用者の到達経路は、既存と同じく各公開型のインスタンスメソッドです。新しい引数や配線は不要で、呼び出し元の変更箇所はありません。追加される入口は `Either#toResult()` と `Result#toEither()` の2つだけです。

**Coder向け実装ガイドライン**

- `Either` には `toResult()` を追加する。
  既存実装パターンは `toOptionRight()` / `toOptionLeft()`（[Either.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Either.java:106)）。
  実装内容は `Left -> Result.failure(value)`, `Right -> Result.success(value)`。
- `Result` には `toEither()` を追加する。
  既存実装パターンは `Try.toEither()`（[Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Try.java:164)）。
  実装内容は `Success -> Either.right(value)`, `Failure -> Either.left(error)`。
- テストは左右/成功失敗の両経路を必ず追加する。
  追加先は `EitherTest` と `ResultTest`。既存の Option 変換テストと同じ粒度に合わせる（[EitherTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/EitherTest.java:75), [ResultTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/ResultTest.java:106)）。
- README には変換方針を一覧で追える形で追記する。
  少なくとも `Either <-> Result`, `Try -> Either`, `Try -> Result`, `Either/Result/Try -> Option` を明示する。既存の型別セクションに追記するか、Features 直下に変換マトリクスを置くのが最小です。
- 新規 public API の Javadoc はリポジトリ方針に従い日本語で書く。
- `Try.java` は既に 331 行で、知識ファイル基準では分割対象ですが、今回の不足 API はありません。スコープ外のため原則触らない方針が妥当です。
- アンチパターンとして、default メソッド追加、`fold` 系 API 追加、別名メソッド増設、既存 API の意味変更は避けるべきです。

**次工程の実施順**

1. `EitherTest` と `ResultTest` に不足変換の失敗/成功両経路テストを先に追加。
2. `Either.java` と `Result.java` に最小 API を実装。
3. `README.md` に変換方針を追記。
4. `mvn test` で全体確認。