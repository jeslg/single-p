package statelesser
package test

import scalaz._

import org.scalatest._

class OptimizationTest extends FlatSpec with Matchers {

  // XXX: Handling the stack is insane! We should take a look at Oleg's
  // implementation. It feels like Shapeless could help here.

  type Stack[A] = 
    ProductProduct[
    ProductProduct[
    ProductProduct[
    ProductProduct[
    ClearId[
    ClearId[
    ClearId[
    ClearId[
    ProductFirst[
    ProductFirst[
    ProductFirst[
    ProductFirst[
    ProductSecond[
    ProductSecond[
    ProductSecond[
    ProductSecond[ 
    VerticalLike[
    VerticalLike[
    VerticalLike[
    VerticalLike[
    Const[String, ?], 
    ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], ?], A]

  val ev19 = VerticalLike.optimization[Const[String, ?]]
  val ev18 = VerticalLike.optimization(ev19)
  val ev17 = VerticalLike.optimization(ev18)
  val ev16 = VerticalLike.optimization(ev17)
  val ev15 = ProductSecond.optimization(ev16)
  val ev14 = ProductSecond.optimization(ev15)
  val ev13 = ProductSecond.optimization(ev14)
  val ev12 = ProductSecond.optimization(ev13)
  val ev11 = ProductFirst.optimization(ev12)
  val ev10 = ProductFirst.optimization(ev11)
  val ev09 = ProductFirst.optimization(ev10)
  val ev08 = ProductFirst.optimization(ev09)
  val ev07 = ClearId.optimization(ev08)
  val ev06 = ClearId.optimization(ev07)
  val ev05 = ClearId.optimization(ev06)
  val ev04 = ClearId.optimization(ev05)
  val ev03 = ProductProduct.optimization(ev04)
  val ev02 = ProductProduct.optimization(ev03)
  val ev01 = ProductProduct.optimization(ev02)
  val ev00 = ProductProduct.optimization(ev01)

  private def wrap[A](s: String): Stack[A] =
    ProductProduct.Unk(
    ProductProduct.Unk(
    ProductProduct.Unk(
    ProductProduct.Unk(
    ClearId.Unk(
    ClearId.Unk(
    ClearId.Unk(
    ClearId.Unk(
    ProductFirst.Unk(
    ProductFirst.Unk(
    ProductFirst.Unk(
    ProductFirst.Unk(
    ProductSecond.Unk(
    ProductSecond.Unk(
    ProductSecond.Unk(
    ProductSecond.Unk(
    VerticalLike.Unk(  
    VerticalLike.Unk(  
    VerticalLike.Unk(  
    VerticalLike.Unk(Const[String, A](s))
    )))))))))))))))))))

  val pretty = new CoupleExample[Stack] {
    type Couple = Unit
    type Person = Unit

    val ev = ev00

    val couples = wrap("couples")
    val her = wrap("her")
    val him = wrap("him")
    val people = wrap("people")
    val name = wrap("name")
    val age = wrap("age")
    val weight = wrap("weight")
  }

  import pretty._

  val exp = "getAll(people > name.asFold1.asFold)"

  def run[A](p: Stack[A]): String =
    ev19.run(
    ev18.run(
    ev17.run(
    ev16.run(
    ev15.run(
    ev14.run(
    ev13.run(
    ev12.run(
    ev11.run(
    ev10.run(
    ev09.run(
    ev08.run(
    ev07.run(
    ev06.run(
    ev05.run(
    ev04.run(
    ev03.run(
    ev02.run(
    ev01.run(
    ev.run(p)))))))))))))))))))).getConst

  "Test optimization stack" should "remove simple first from query" in {
    run(getPeopleName_2) shouldBe exp
  }

  it should "remove several firsts from query" in {
    run(getPeopleName_3) shouldBe exp
  }

  it should "remove a chain of composed firsts" in {
    run(getPeopleName_4) shouldBe exp
  }

  it should "remove a first which contains a continuation" in {
    run(getPeopleName_5) shouldBe exp
  }

  it should "reduce a quite dummy query" in {
    run(getPeopleName_5) shouldBe exp
  }

  val exp2 = "getAll(people > (name * likeInt(3)).asFold1.asFold)"

  it should "reduce a query with likes" in {
    run(getPeopleNameAnd3_1) shouldBe exp2
  }

  it should "reduce a quite dummy query with likes" in {
    run(getPeopleNameAnd3_2) shouldBe exp2
  }
}

