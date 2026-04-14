対象タスクは `Try` / `Result` の `recover` / `recoverWith` 実装で、まず一次情報の指示書・レポート・既存コードと既存テストを突き合わせます。変更前にスコープ宣言を作成し、その後に実装箇所を最小限で確定します。
レポート側は `coder-scope.md` と `coder-decisions.md` がまだ未作成でした。次に実装対象の `Try` / `Result` と、すでに追加済みの `TryTest` / `ResultTest` を読み、期待挙動をコード上で固定します。
期待値は明確でした。成功側は同一インスタンスを返し、失敗側だけ mapper を評価、`null` mapper は `NullPointerException`、`null` 戻り値は `IllegalArgumentException` です。先にレポートディレクトリへスコープ宣言を追加し、その後に `Try` / `Result` 本体だけを編集します。
実装方針は固まりました。`Success` 側は no-op で `this` を返し、`Failure` 側だけ mapper を評価する形で既存 `map` / `flatMap` の流儀に合わせます。あわせて、受け入れ条件にある README の使用例も最小限で追記します。
これから本体実装を入れます。編集対象は `Try.java`、`Result.java`、`README.md` のみで、テストファイルには触れません。
実装は入りました。次に差分を目視で確認して自己チェックを済ませたうえで、`mvn compile` と `mvn test` を順に実行します。
コンパイルを実行中です。Maven/JDK 由来の警告は出ていますが、現時点では失敗理由ではなく、ビルド継続中です。
ビルドと全テストは通りました。最後に変更範囲だけ確認して、出力用に要約を整理します。
## 作業結果
- `Try` / `Result` に `recover` / `recoverWith` を実装し、失敗側だけ mapper を評価して復旧するようにしました。
- 成功側は既存インスタンスをそのまま返す no-op にし、追加済みテストの期待どおり `null mapper` は active 側で `NullPointerException`、`null` 戻り値は `IllegalArgumentException` になるように合わせました。
- 実装開始時のスコープ宣言を [coder-scope.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/.takt/runs/20260414-042835-issue-4-try-result-api-issue-t/reports/coder-scope.md) に作成しました。

## 変更内容
- [Try.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java) に `recover` / `recoverWith` を追加し、`Success` は `this` を返却、`Failure` は `Try.success(...)` または mapper の返す `Try` で復旧する実装を追加。
- [Result.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java) に同名 API を追加し、`Success` は `this`、`Failure` は `Result.success(...)` または mapper の返す `Result` で復旧する実装を追加。
- [README.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md) に `Result` / `Try` の復旧例を追記。
- 既存テストファイルの変更はこのステップ以前の `write_tests` で入っていたもので、今回はプロダクションコードと README のみ編集しました。

## ビルド結果
- `mvn -q -DskipTests compile` : 成功
- Maven/JDK の警告は出ていますが、ビルド失敗ではありません。

## テスト結果
- `mvn test` : 成功
- 実行結果: `Tests run: 73, Failures: 0, Errors: 0, Skipped: 0`