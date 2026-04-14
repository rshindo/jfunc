## Issue #4: Try / Result に失敗からの復旧 API を追加する

# Issue: `Try` / `Result` に失敗からの復旧 API を追加する

## 背景
- `Try` は例外を `Failure` に閉じ込める用途、`Result` は ROP 的な成功/失敗の分岐に使う用途として整理されている。
- 現状は `map` / `flatMap` / `onSuccess` / `onFailure` を中心とした最小 API になっている。
- 実際の利用では、失敗時にデフォルト値へ戻す、失敗理由を別の失敗へ写像する、条件付きでリトライ可能な処理へ戻す、といった復旧が頻出する。

## 課題
- 失敗からの復旧を表現するのに `switch` で毎回分岐を書く必要がある。
- 成功パスの連鎖は簡潔だが、失敗パスの回復はやや書きづらい。
- Java の I/O や parse 失敗を扱うユースケースで、`Try` の実用性をもう一段上げたい。

## 提案
- `Try` に以下のような復旧 API を追加検討する。
  - `recover(Function<? super Throwable, ? extends T>)`
  - `recoverWith(Function<? super Throwable, Try<T>>)`
- `Result` についても同様に、失敗値からの復旧 API を追加検討する。
  - `recover(Function<? super E, ? extends T>)`
  - `recoverWith(Function<? super E, Result<T, E>>)` または同等 API
- 既存の右バイアス設計を維持し、成功経路はそのまま、失敗経路のみを扱う API に限定する。

## スコープ案
- `fold` や包括的な分岐 API は追加しない。
- まずは最小限の復旧 API に絞る。
- `null` を返した場合の扱いは既存ポリシーに合わせて明確化する。

## 受け入れ条件
- `Try.Success` / `Try.Failure` の双方についてテストがある。
- `Result.Success` / `Result.Failure` の双方についてテストがある。
- mapper が `null` を返す場合や mapper 自体が `null` の場合の挙動が定義・テストされている。
- README に典型例外処理やバリデーションの例が追加されている。

## 補足
- 特に `Try.recover` 系は、Java 利用者にとって直感的な価値が高い。
