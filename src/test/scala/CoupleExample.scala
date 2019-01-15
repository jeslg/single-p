package statelesser
package core
package test

import scalaz._, Scalaz._

import optic._
import sqlnormal._
import core.Statelesser, Statelesser._

trait CoupleExample[Expr[_]] {

  implicit val ev: Statelesser[Expr]

  /* data layer */

  type Couple
  type Couples = List[Couple]
  type Person
  type People = List[Person]
  type Address

  def couples: Expr[Fold[Couples, Couple]]
  def her: Expr[Getter[Couple, Person]]
  def him: Expr[Getter[Couple, Person]]
  def people: Expr[Fold[People, Person]]
  def name: Expr[Getter[Person, String]]
  def age: Expr[Getter[Person, Int]]
  def weight: Expr[Getter[Person, Int]]
  def address: Expr[Getter[Person, Address]]
  def aliases: Expr[Fold[Person, String]]
  def street: Expr[Getter[Address, String]]

  /* logic */

  import ev._
  import Statelesser.syntax._

  def getPeople: Expr[Fold[People, Person]] =
    people

  def getPeopleName_1: Expr[Fold[People, String]] =
    people > name.asFold

  def getPeopleName_2: Expr[Fold[People, String]] =
    people > ((name * age) > first).asFold

  def peopleName_1: Expr[Fold[People, String]] =
    people > (name * age * weight > first > first).asFold

  def peopleName_2: Expr[Fold[People, String]] =
    people > (
      name * weight * age * name * weight >
      id >
      second * first >
      second * first >
      first * id >
      first >
      second).asFold

  // def getPeopleName_3: Expr[People => List[String]] =
  //   getAll(people > (((name * age) * weight > first) > first).asFold)

  // def getPeopleName_4: Expr[People => List[String]] =
  //   getAll(people > (((name * age) * weight > (first > first))).asFold)

  // def getPeopleName_5: Expr[People => List[String]] =
  //   getAll(people > (name * (age * weight) > (first > id)).asFold)

  // def getPeopleName_6: Expr[People => List[String]] =
  //   getAll(people >
  //     (name * weight * age * name * weight >
  //     id >
  //     second * first >
  //     second * first >
  //     first * id >
  //     first >
  //     second).asFold)

  def getPeopleNameAnd3_1: Expr[Fold[People, (String, Int)]] =
    people > (name * likeInt(3)).asFold

  def getPeopleNameAnd3_2: Expr[Fold[People, (String, Int)]] =
    people > (
      name * weight * age * name * weight >
      id >
      first * likeInt(3) >
      (first[(((String, Int), Int), String), Int] > second) * second >
      id).asFold

  def getPeopleNameAnd3_3: Expr[Fold[People, (String, Int)]] =
    people > (
      (name * (likeInt(3) * likeInt(0))) >
      first * (second > sub)).asFold

  def getPeopleNameAnd3_4: Expr[Fold[People, (String, Int)]] =
    people > (
      (name * (likeInt(4) * likeInt(1))) >
      first * (second > sub)).asFold

  def getPeopleNameAndAge_1: Expr[Fold[People, (String, Int)]] =
    people > (name * age).asFold

  def getPeopleNameAndAge_2: Expr[Fold[People, (String, Int)]] =
    people > ((name * age * weight) > first).asFold

  def getHer: Expr[Fold[Couples, Person]] =
    couples > her.asFold

  def getHerAndHim: Expr[Fold[Couples, (Person, Person)]] =
    couples > (her * him).asFold

  def herNameAndStreet: Expr[Fold[Couples, (String, String)]] =
    couples > (her > id > name * (address > street)).asFold

  def herAndHimStreet_1: Expr[Fold[Couples, (String, String)]] =
    couples > (
      her * him > (first > address > street) * (second > address > street)).asFold

  def getHerName: Expr[Fold[Couples, String]] =
    couples > her.asFold > name.asFold

  def getHerNameAndAge_1: Expr[Fold[Couples, (String, Int)]] =
    couples > her.asFold > (name * age).asFold

  def getHerNameAndAge_2: Expr[Fold[Couples, (String, Int)]] =
    couples > ((her > id > name) * (her > age)).asFold

  def getHerNameAndAge_3: Expr[Fold[Couples, (String, Int)]] =
    couples > (id > her > name * age > id > id).asFold

  def getHerNameAndHimAliases_1: Expr[Fold[Couples, (String, String)]] =
    couples > (her > name).asFold * (him.asFold > aliases)

  def getPeopleGt30: Expr[Fold[People, (String, Int)]] =
    people > (name.asAffineFold *
      (age.asAffineFold > filtered (gt(30)))).asFold

  def getHerGt30_1: Expr[Fold[Couples, (String, Int)]] =
    couples > her.asFold > (name.asAffineFold *
      (age.asAffineFold > filtered (gt(30)))).asFold

  def getHerGt30_2: Expr[Fold[Couples, (String, Int)]] =
    couples > ((her > name).asAffineFold *
      ((her > age).asAffineFold > filtered (gt(30)))).asFold

  def getHerNameGt30_1: Expr[Fold[Couples, String]] =
    couples > her.asFold > (name.asAffineFold <*
      (age.asAffineFold > filtered (gt(30)))).asFold

  def getHerNameGt30_2: Expr[Fold[Couples, String]] =
    couples > ((her > name).asAffineFold <*
      ((her > age).asAffineFold > filtered (gt(30)))).asFold

  // def differenceAll: Expr[Couples => List[(String, Int)]] =
  //   getAll(couples >
  //     ((her > name) * ((her > age) * (him > age) > sub)).asFold)

  def difference: Expr[Fold[Couples, (String, Int)]] =
    couples >
      ((her > name).asAffineFold *
        (((her > age) - (him > age)).asAffineFold > filtered (gt(0)))).asFold

  def difference_1: Expr[Fold[Couples, (String, Int)]] =
    (couples > (her > name).asFold) *
    (couples > (((her > age) - (him > age)).asAffineFold > filtered (gt(0))).asFold)

  def differenceName_1: Expr[Fold[Couples, String]] =
    couples >
      ((her > name).asAffineFold <*
        (((her > age) - (him > age)).asAffineFold > filtered (gt(0)))).asFold

  def differenceName_2: Expr[Fold[Couples, String]] =
    couples >
      ((her > name).asAffineFold *
        (((her > age) - (him > age)).asAffineFold > filtered (gt(0))) >
          first.asAffineFold).asFold

  def dummyNameAndAge: Expr[Fold[People, (String, Int)]] =
    people > ((name.asAffineFold * ((name * age >
      first * second >
      second * first >
      second * first >
      second).asAffineFold
      > filtered(gt(30))
      > filtered(
          id * (likeInt[Int](41) * likeInt[Int](1) > sub)
            > greaterThan)))).asFold
}

object CoupleExample {

  val instance: CoupleExample[Semantic] = new CoupleExample[Semantic] {

    val ev = Statelesser[Semantic]

    def assignRoot[O[_, _], S, A](ot: OpticType[S, A]): Semantic[O[S, A]] = 
      fresh.map(s => Done(Just(Var(s)), Set.empty, Map(s -> ot.left)))

    def assignNode[O[_, _], S, A](ot: OpticType[S, A]): Semantic[O[S, A]] =
      fresh.map(s => Todo(λ[Done[O, ?, S] ~> Done[O, ?, A]] { done =>
        done.expr match {
          case Just(x@Var(_)) =>
            Done(Just(Var(s)), done.filt, done.vars + (s -> Select(x, ot).right))
        }
      }))

    def assignLeaf[O[_, _], S, A](otpe: OpticType[S, A]): Semantic[O[S, A]] =
      state(Todo(λ[Done[O, ?, S] ~> Done[O, ?, A]] { done =>
        done.expr match {
          case Just(x@Var(_)) => done.copy(expr = Just(Select(x, otpe)))
        }
      }))

    val couples = assignRoot(
      OpticType(KFold, "couples", TypeInfo("Couples"), TypeInfo("Couple", true)))

    val her = assignNode(
      OpticType(KGetter, "her", TypeInfo("Couple", true), TypeInfo("Person", true)))

    val him = assignNode(
      OpticType(KGetter, "him", TypeInfo("Couple", true), TypeInfo("Person", true)))

    val people = assignRoot(
      OpticType(KFold, "people", TypeInfo("People"), TypeInfo("Person", true)))

    val name = assignLeaf(
      OpticType(KGetter, "name", TypeInfo("Person", true), TypeInfo("String")))

    val age = assignLeaf(
      OpticType(KGetter, "age", TypeInfo("Person", true), TypeInfo("Int")))

    val weight = assignLeaf(
      OpticType(KGetter, "weight", TypeInfo("Person", true), TypeInfo("Int")))

    val address = assignNode(
      OpticType(KGetter, "address", TypeInfo("Person", true), TypeInfo("Address", true)))

    val aliases = assignNode(
      OpticType(KFold, "aliases", TypeInfo("Person", true), TypeInfo("String")))

    val street = assignLeaf(
      OpticType(KGetter, "street", TypeInfo("Address", true), TypeInfo("String")))
  }
}
