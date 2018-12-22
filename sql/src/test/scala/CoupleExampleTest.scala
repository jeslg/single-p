package statelesser
package sql
package test

import scala.util.matching.Regex
import scalaz._, Scalaz._
import OpticLang.Table
import statelesser.test._
import org.scalatest._

class CoupleExampleTest extends FlatSpec with Matchers {

  import CoupleExample._, instance._
  import SQL._

  val keys = Map("Person" -> "name")

  def genSql[S, A](sem: Semantic[Const[String, ?], Fold[S, A]]): String =
    sqlToString(fromSemantic(sem, keys))

  def matchSql[S, A](r: Regex, stks: Stack[Fold[S, A]]*) =
    stks.foreach { stk => 
      genSql(stk) should fullyMatch regex r
    } 

  "Statelesser" should "generate wildcard '*' selection" in {
    matchSql(raw"SELECT (.+)\.\* FROM Person AS \1;".r, getPeople)
  }

  it should "generate specific selection" in {
    matchSql(raw"SELECT (.+)\.name FROM Person AS \1;".r, getPeopleName_1)
  }

  it should "generate multi-selection" in {
    matchSql(
      raw"SELECT (.+)\.name, \1\.age FROM Person AS \1;".r, 
      getPeopleNameAndAge_1,
      getPeopleNameAndAge_2)
  }

  it should "generate wildcard nested selection" in {
    matchSql(
      raw"SELECT (.+)\.\* FROM Couple AS (.+) INNER JOIN Person AS \1 ON \2\.her = \1\.name;".r, 
      getHer)
  }

  it should "generate multiple wildcard nested selection" in {
    matchSql(
      raw"SELECT (.+)\.\*, (.+)\.\* FROM Couple AS (.+) INNER JOIN Person AS \1 ON \3\.her = \1\.name INNER JOIN Person AS \2 ON \3\.him = \2\.name;".r, 
      getHerAndHim)
  }

  it should "generate nested specific selection" in {
    matchSql(
      raw"SELECT (.+)\.name FROM Couple AS (.+) INNER JOIN Person AS \1 ON \2\.her = \1\.name;".r,
      getHerName)
  }

  it should "generate nested multi-selection" in {
    matchSql(
      raw"SELECT (.+)\.name, \1\.age FROM Couple AS (.+) INNER JOIN Person AS \1 ON \2\.her = \1\.name;".r,
      getHerNameAndAge_1, 
      getHerNameAndAge_2, 
      getHerNameAndAge_3)
  }

  it should "generate multi-selection with literals" in {
    matchSql(
      raw"SELECT (.+)\.name, 3 FROM Person AS \1;".r,
      getPeopleNameAnd3_1,
      getPeopleNameAnd3_2,
      getPeopleNameAnd3_3,
      getPeopleNameAnd3_4)
  }

  it should "generate filters" in {
    matchSql(
      raw"SELECT (.+)\.name, \1\.age FROM Person AS \1 WHERE \(\1\.age > 30\);".r, 
      getPeopleGt30)
  }

  it should "generate filters for nested fields" in {
    matchSql(
      raw"SELECT (.+)\.name, \1\.age FROM Couple AS (.+) INNER JOIN Person AS \1 ON \2\.her = \1\.name WHERE \(\1\.age > 30\);".r,
      getHerGt30_1, 
      getHerGt30_2)
  }

  it should "generate remove filtering fields from select" in {
    matchSql(
      raw"SELECT (.+)\.name FROM Couple AS (.+) INNER JOIN Person AS \1 ON \2\.her = \1\.name WHERE \(\1\.age > 30\);".r,
      getHerNameGt30_1, 
      getHerNameGt30_2)
  }

  it should "generate complex queries" in {
    matchSql(
      raw"SELECT (.+)\.name, \(\1\.age - (.+)\.age\) FROM Couple AS (.+) INNER JOIN Person AS \1 ON \3\.her = \1\.name INNER JOIN Person AS \2 ON \3\.him = \2\.name WHERE \(\(\1\.age - \2\.age\) > 0\);".r,
      difference)

    matchSql(
      raw"SELECT (.+)\.name FROM Couple AS (.+) INNER JOIN Person AS \1 ON \2\.her = \1\.name INNER JOIN Person AS (.+) ON \2\.him = \3\.name WHERE \(\(\1\.age - \3\.age\) > 0\);".r,
      differenceName_1, 
      differenceName_2)
  }

  it should "normalise a stupid query" in {
    matchSql(
      raw"SELECT (.+)\.name, \1\.age FROM Person AS \1 WHERE \(\(\1\.age > 30\) AND \(\1\.age > 40\)\);".r, 
      dummyNameAndAge)
  }
}
