今回のステップは `write_tests` なので、まず指示書・ポリシー・計画レポートと対象の既存コード/既存テストを読み、追加すべきテストケースだけに絞ります。プロダクションコードは触らず、テストファイルだけを更新します。
要件は `recover` / `recoverWith` の追加で、`null` と `null mapper` の挙動までテスト対象です。次に `plan.md` と現行の `Try` / `Result` / 既存テストを突き合わせて、テスト名と期待例外を固定します。
既存テストの一覧取得が浅すぎたので、`src/test/java` 配下を正しく取り直します。`TryTest` と `ResultTest` の実際の記法を見て、それに合わせて不足ケースだけ追加します。
インテグレーションテストは不要です。今回の追加は `Try` と `Result` 各単体の新規 API で、3モジュール以上を横断する新しいデータフローや状態合流はありません。これから既存の `TryTest` / `ResultTest` に、復旧 API の正常系・失敗系・`null` 系を追加します。
テスト追加は完了しました。次に `mvn test` を実行して、未実装 API による想定どおりの失敗か、テストコード側の不整合がないかを確認します。
`Try` / `Result` の復旧 API 向けテストを [`TryTest.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/test/java/com/github/rshindo/jfunc/TryTest.java) と [`ResultTest.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/test/java/com/github/rshindo/jfunc/ResultTest.java) に追加しました。対象は `recover` / `recoverWith` の成功側 no-op、失敗側の復旧、`null mapper`、`null` 戻り値です。インテグレーションテストは不要と判断しています。

`mvn test` は実行済みで、想定どおり未実装の `recover` / `recoverWith` が `Try` と `Result` に存在しないため `testCompile` で失敗しています。テストコード側の構文不整合ではなく、次の `implement` ステップで本体 API を追加すれば進められる状態です。