# Chapter 23 — Testing

> Three serious testing options in 2026: ScalaTest, MUnit, and ScalaCheck. This chapter shows what each is for and how to test async code, actors, and properties.

**In this chapter:**
1. [ScalaTest](#1-scalatest)
2. [MUnit](#2-munit)
3. [ScalaCheck (property-based testing)](#3-scalacheck-property-based-testing)
4. [Mocking](#4-mocking)
5. [Testing async code (Futures, IO)](#5-testing-async-code-futures-io)
6. [Testing actors (Akka TestKit)](#6-testing-actors-akka-testkit)

---

## 1. ScalaTest

The most full-featured testing framework in Scala. Multiple "styles" let you pick the syntax you like:

```scala
// build.sbt
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
```

### FunSuite (xUnit style)

```scala
import org.scalatest.funsuite.AnyFunSuite

class MathSpec extends AnyFunSuite {
  test("addition") {
    assert(2 + 2 == 4)
  }

  test("division by zero") {
    assertThrows[ArithmeticException] {
      1 / 0
    }
  }
}
```

### FlatSpec (BDD-ish)

```scala
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CalculatorSpec extends AnyFlatSpec with Matchers {
  "A Calculator" should "add two numbers" in {
    (2 + 2) shouldBe 4
  }

  it should "throw on divide by zero" in {
    an[ArithmeticException] should be thrownBy (1 / 0)
  }
}
```

### WordSpec (more nesting)

```scala
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class UserServiceSpec extends AnyWordSpec with Matchers {
  "UserService" when {
    "given a valid id" should {
      "return a User" in {
        UserService.find("1") shouldBe Some(User("Bhaskar"))
      }
    }
    "given an invalid id" should {
      "return None" in {
        UserService.find("999") shouldBe None
      }
    }
  }
}
```

### Matchers

ScalaTest's `Matchers` trait gives you the readable `shouldBe`, `shouldEqual`, `should contain`, etc. Useful ones:

```scala
1 shouldBe 1
"foo" should startWith ("f")
List(1, 2, 3) should contain (2)
List(1, 2, 3) should have size 3
Map("a" -> 1) should contain key "a"
None shouldBe empty
result should equal (expected) (after being normalized(_.trim))
```

`assertResult(expected) { actual }` for cases without `Matchers`.

---

## 2. MUnit

MUnit is the lighter, modern alternative — fewer features, faster startup, simpler concepts.

```scala
// build.sbt
libraryDependencies += "org.scalameta" %% "munit" % "1.0.1" % Test

testFrameworks += new TestFramework("munit.Framework")
```

```scala
import munit.FunSuite

class MathSpec extends FunSuite {
  test("addition") {
    assertEquals(2 + 2, 4)
  }

  test("division") {
    intercept[ArithmeticException] { 1 / 0 }
  }
}
```

API surface is small: `assert`, `assertEquals`, `assertNotEquals`, `assertNoDiff`, `intercept`, `interceptMessage`. No DSL, no styles. If your team prefers minimal toolchains, MUnit is great.

MUnit also has built-in support for `Future`, `IO`, `Resource`, fixtures, and async tests — natively, without extra libraries.

---

## 3. ScalaCheck (property-based testing)

Generate test cases from properties:

```scala
// build.sbt
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.18.0" % Test
```

```scala
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object StringProps extends Properties("String") {
  property("reverse twice == identity") = forAll { (s: String) =>
    s.reverse.reverse == s
  }

  property("length stable under reverse") = forAll { (s: String) =>
    s.length == s.reverse.length
  }
}
```

ScalaCheck generates many random `String`s and checks the property. If it fails, it shrinks to a small reproducer.

Integration with ScalaTest:

```scala
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class MathProps extends AnyPropSpec with ScalaCheckPropertyChecks {
  property("addition is commutative") {
    forAll { (a: Int, b: Int) =>
      assert(a + b == b + a)
    }
  }
}
```

Property tests catch edge cases (empty strings, negative numbers, integer overflow) you'd never think to write manually. Worth using for any function with general inputs.

---

## 4. Mocking

Three approaches:

**1. Hand-rolled stubs** (the FP-favored approach):

```scala
trait UserRepo {
  def find(id: String): Option[User]
}

class FakeUserRepo(users: Map[String, User]) extends UserRepo {
  def find(id: String): Option[User] = users.get(id)
}

val repo = new FakeUserRepo(Map("1" -> User("Bhaskar")))
```

Just write a small implementation. No mocking library, no DSL. Tends to be the cleanest — *especially* for traits with few methods.

**2. Mockito-Scala**:

```scala
// libraryDependencies += "org.mockito" %% "mockito-scala" % "1.17.31" % Test
import org.mockito.IdiomaticMockito

class UserSpec extends AnyFlatSpec with Matchers with IdiomaticMockito {
  "UserService" should "delegate to repo" in {
    val repo = mock[UserRepo]
    repo.find("1") returns Some(User("X"))

    val svc = new UserService(repo)
    svc.greet("1") shouldBe "Hello, X"

    repo.find("1") was called
  }
}
```

Mockito is pragmatic for OO-style code with many collaborators.

**3. EasyMock / ScalaMock** — older alternatives, still used. Pick one and stick with it.

The FP-leaning advice: hand-rolled stubs > Mockito > runtime reflection mocks. The more "magic" the framework, the harder the tests are to read later.

---

## 5. Testing async code (Futures, IO)

### `Future` with ScalaTest

```scala
import org.scalatest.flatspec.AsyncFlatSpec
import scala.concurrent.Future

class FutureSpec extends AsyncFlatSpec {
  "fetch" should "return user" in {
    fetchUser("1").map { user =>
      assert(user.name == "Bhaskar")
    }
  }
}
```

`AsyncFlatSpec` returns `Future[Assertion]` from each test. ScalaTest awaits each.

### `IO` (Cats Effect) with MUnit

MUnit has built-in `IO` support:

```scala
import munit.CatsEffectSuite
import cats.effect.IO

class MyAppSpec extends CatsEffectSuite {
  test("io test") {
    for {
      _ <- IO.println("running test")
      n <- IO.pure(42)
    } yield assertEquals(n, 42)
  }
}
```

(Add `"org.typelevel" %% "munit-cats-effect" % "2.0.0"` for `CatsEffectSuite`.)

ScalaTest also has Cats Effect integration via `cats-effect-testing`:

```scala
class MyAppSpec extends AsyncIOSpec with Matchers {
  "io test" should "work" in {
    IO.pure(42).asserting(_ shouldBe 42)
  }
}
```

### `Future` testing without async helpers

If you must, `Await.result` works in tests (it's a boundary). But prefer the async variants — they let you describe expectations as `Future[Assertion]`.

---

## 6. Testing actors (Akka TestKit)

For Akka Typed:

```scala
// build.sbt
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.8.5" % Test
```

```scala
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class GreeterSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A Greeter" must {
    "log on Greet" in {
      val probe = createTestProbe[String]()
      val greeter = spawn(Greeter())

      greeter ! Greeter.Greet("Bhaskar", probe.ref)

      probe.expectMessage("hello, Bhaskar")
    }
  }
}
```

`createTestProbe` gives you a synchronous "actor" that captures messages — assert what your actor sent. `spawn` creates the actor under test. The `TestKit` wires up an `ActorSystem` and tears it down after.

For Akka Streams, similar `TestKit` provides `TestSource` / `TestSink` to drive a graph manually.

---

## What you should now know

- ScalaTest with three styles: FunSuite, FlatSpec, WordSpec.
- MUnit as the lighter alternative.
- ScalaCheck for property-based tests.
- Three mocking approaches; preference order from most-to-least preferred (FP-leaning).
- Async testing with `AsyncFlatSpec` (Future) and `CatsEffectSuite` (IO).
- Akka TestKit for actors.

The next chapter ([Chapter 24 — Java Interop](24-java-interop.md)) covers calling Java from Scala and exposing Scala to Java.

---

[← Previous: Chapter 22 — sbt](22-build-tools-sbt.md) | [Back to README](README.md) | [Next: Chapter 24 — Java Interop →](24-java-interop.md)
