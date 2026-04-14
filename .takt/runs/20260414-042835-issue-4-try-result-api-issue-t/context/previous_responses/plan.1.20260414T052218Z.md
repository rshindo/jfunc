依頼内容を確認し、まずタスク指示書と参照資料を実際に読みます。その上で現行実装との差分と影響範囲を特定し、実装者向けの具体的な方針まで落とし込みます。
`rg` がないため、参照資料と実行履歴は `find` で列挙します。まずタスク指示書、参照資料、既存のレポート履歴を確認して、今回のソース・オブ・トゥルースを固定します。
仕様は `order.md` だけで、参照資料の追加指定は見当たりません。次に現行実装の `Try` / `Result` / テスト / README を読み、要件ごとに現状で不足している点を切り分けます。
現行の `Try` / `Result` には復旧 API がまだなく、README にも復旧例はありません。次に、同系 API の命名・null 方針・実装位置を既存型から確認して、実装時に揃えるべきパターンを固定します。
必要な事実は揃ってきました。ここからは計画で使う根拠を固めるため、変更候補箇所の行番号と既存パターンを拾います。
ここまでで、前回レポートは存在せず、今回の判断材料は `order.md` と現行コードのみです。実装対象は小さめですが、`recoverWith` の型と null 振る舞いは明示しておく必要があるため、設計判断込みで整理します。
**調査結果**

参照資料は [`order.md`](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/.takt/runs/20260414-042835-issue-4-try-result-api-issue-t/context/task/order.md>) のみでした。レポート履歴は `Report Directory` にファイルがなく、今回の計画はこの指示書と現行実装の読解に基づきます。

要件ごとの判定です。

- `Try` に失敗復旧 API を追加: 変更要。現状の公開 API は `map` / `flatMap` / `onSuccess` / `onFailure` / 変換系のみで、復旧 API は未実装です。[Try.java:113](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:113>)-[Try.java:176](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:176>)
- `Result` に失敗復旧 API を追加: 変更要。現状は `mapFailure` で失敗値の変換はできますが、失敗から成功へ戻す API はありません。[Result.java:61](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:61>)-[Result.java:125](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:125>)
- `fold` など包括分岐 API は追加しない: 変更不要。現状も pattern matching 優先で `fold` はありません。[Try.java:10](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:10>)-[Try.java:13](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:13>)、[Result.java:11](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:11>)-[Result.java:20](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:20>)
- `null` mapper / `null` 戻り値の挙動定義とテスト: 変更要。既存テストには該当ケースがありません。[TryTest.java:13](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/test/java/com/github/rshindo/jfunc/TryTest.java:13>)、[ResultTest.java:12](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/test/java/com/github/rshindo/jfunc/ResultTest.java:12>)
- README に典型例追加: 変更要。`Try` / `Result` の説明とサンプルはありますが、復旧 API の記述はありません。[README.md:14](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md:14>)-[README.md:21](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md:21>)、[README.md:111](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md:111>)-[README.md:207](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md:207>)

**設計方針**

今回の追加 API は最小構成で十分です。

- `Try<T>`:
  - `T recover(Function<? super Throwable, ? extends T>)` ではなく、既存 API と揃えて `Try<T> recover(...)` にし、失敗時のみ `Success` へ戻す形にする
  - `Try<T> recoverWith(Function<? super Throwable, Try<T>> mapper)`
- `Result<T,E>`:
  - `Result<T,E> recover(Function<? super E, ? extends T> mapper)`
  - `Result<T,E> recoverWith(Function<? super E, Result<T,E>> mapper)`

理由は、`order.md` の意図が「成功経路はそのまま、失敗経路のみを扱う API」にあることと、既存の `map` / `flatMap` / `mapFailure` がすべて「同じコンテナ型を返す」設計だからです。[Try.java:113](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:113>)、[Result.java:61](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:61>)

null 方針は既存の active-side 実装に合わせるのが安全です。

- `recover`: active failure 側で mapper を適用し、`null` を返したら `IllegalArgumentException`
- `recoverWith`: active failure 側で mapper を適用し、`null` を返したら `IllegalArgumentException`
- inactive success 側では mapper を評価せず、そのまま `this` を返す
- mapper 自体が `null` の場合は active failure 側で `NullPointerException`、inactive success 側では例外なし

この方針は、既存の `map` / `mapFailure` / `flatMap` が「使う側の variant でのみ mapper を触る」実装になっていることに整合します。[Try.java:194](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:194>)、[Try.java:273](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:273>)、[Result.java:138](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:138>)、[Result.java:198](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:198>)

**Coder 向け実装ガイドライン**

- 変更対象は [`Try.java`](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java>)、[`Result.java`](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java>)、[`TryTest.java`](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/test/java/com/github/rshindo/jfunc/TryTest.java>)、[`ResultTest.java`](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/test/java/com/github/rshindo/jfunc/ResultTest.java>)、[`README.md`](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md>) の 5 ファイルで足ります。
- 既存実装パターンは `Success` 側で mapper を適用する `map`、`Failure` 側で mapper を適用する `mapFailure` をそのまま踏襲してください。[Try.java:193](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:193>)、[Result.java:197](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Result.java:197>)
- `recoverWith` の null 戻り値は必ず明示的に弾いてください。`flatMap` と同じく素通しにすると、受け入れ条件の「挙動定義」に不足が残ります。
- `Try.java` は既に 331 行ありますが、このプロジェクトでは sum type を sealed interface + nested record で同居させる方針です。今回は分割計画を入れず、同ファイル内で完結させるのが妥当です。[Try.java:18](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/src/main/java/com/github/rshindo/jfunc/Try.java:18>)、AGENTS の `Option` / `Either` 設計方針
- テストは双方の variant を分けて追加してください。
  - `Try.Success` では no-op で元のインスタンスが返ること
  - `Try.Failure` では `recover` が `Success`、`recoverWith` が任意の `Try` に復旧できること
  - `Result.Success` では no-op
  - `Result.Failure` では `recover` が `Success`、`recoverWith` が `Success` / `Failure` の両方を返せること
  - active-side の `null mapper` と `null return` の例外
  - inactive-side の `null mapper` が評価されないこと
- README では利用者到達経路として、既存の `Result` パイプライン例と `Try.of(...)` 例にそれぞれ 1 つずつ復旧サンプルを足すのが最短です。[README.md:117](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md:117>)、[README.md:152](</Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0518-4-issue-4-try-result-ni-karano-a/README.md:152>)
- このタスクで避けるべきアンチパターンは 2 つです。
  - `fold` や `getOrElse` のような追加拡張を便乗で入れること
  - 既存の `map` / `flatMap` / `mapFailure` の null セマンティクスまで触ってスコープを広げること

次工程は、まずテスト追加で API セマンティクスを固定し、その後に `Try` / `Result` 本体、最後に README 更新の順が適切です。