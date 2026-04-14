今回のステップでは、`.takt/.../context/task` の指示と指定されたレポート、既存の実装・テストを確認し、プロダクションコードには触れずに先行テストだけを追加します。まず仕様書、ポリシー全文、既存テストの形を実ファイルで固めます。
仕様本体とポリシーは確認できました。次に、このステップで参照を許されているレポート `plan.md` と、対象の既存実装・既存テストを読んで追加すべき失敗テストを確定します。
既存テストの書き方と、今回の計画で固定された `null` 契約・短絡評価の要件は確認できました。ここから `OptionTest` と `ResultTest` にだけ、`sequence` / `traverse` 向けの失敗前提テストを追加します。
`OptionTest` に入れた短絡評価テストは、要素生成時に副作用が走る形だと要件を正しく固定できません。そこをイテレータ走査時に副作用が出る形へ直してから、`ResultTest` も同様に追加します。
`null` 要素テストは `List.of` だとテスト対象に到達する前に JDK 側で落ちます。そこを API 契約の検証になるよう差し替えて、あわせて `null` iterable の型推論も明示します。
テスト追加は終わったので、ここで `mvn test` を実行して、未実装由来の失敗とテストコード側の不備を切り分けます。失敗がテスト記述の問題ならこのステップ内で直します。
`mvn test` の失敗は想定どおりで、追加した `sequence` / `traverse` が未実装なことによるコンパイルエラーに限定されました。テストコード自体の構文や `null` ケースの組み立ては通っており、このステップでの修正は不要です。
`sequence` / `traverse` の先行テストを [OptionTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/src/test/java/com/github/rshindo/jfunc/OptionTest.java:118) と [ResultTest.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/src/test/java/com/github/rshindo/jfunc/ResultTest.java:153) に追加しました。対象は空入力、全成功、途中失敗、短絡評価、`null` iterable、`null` 要素、`null` mapper、mapper の `null` 戻り値です。インテグレーションテストは、今回の変更が 3 モジュール以上をまたぐデータフローではないため追加していません。

`mvn test` は実行済みで、失敗内容は想定どおり `Option.sequence` / `Option.traverse` / `Result.sequence` / `Result.traverse` が未実装なことによるテストコンパイルエラーに限定されました。次の `implement` ステップで本体実装に進める状態です。