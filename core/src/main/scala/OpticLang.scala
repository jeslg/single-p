package statelesser

import scalaz._, Scalaz._
import Leibniz._

trait OpticLang[Expr[_]] {

  def flVert[S, A, B](
    l: Expr[Fold[S, A]], 
    r: Expr[Fold[A, B]]): Expr[Fold[S, B]]

  def flHori[S, A, B](
    l: Expr[Fold[S, A]], 
    r: Expr[Fold[S, B]]): Expr[Fold[S, (A, B)]]

  def gtVert[S, A, B](
    l: Expr[Getter[S, A]], 
    r: Expr[Getter[A, B]]): Expr[Getter[S, B]]

  def gtHori[S, A, B](
    l: Expr[Getter[S, A]],
    r: Expr[Getter[S, B]]): Expr[Getter[S, (A, B)]]

  def aflVert[S, A, B](
    l: Expr[AffineFold[S, A]], 
    r: Expr[AffineFold[A, B]]): Expr[AffineFold[S, B]]

  def aflHori[S, A, B](
    l: Expr[AffineFold[S, A]],
    r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, (A, B)]]

  def filtered[S](p: Expr[Getter[S, Boolean]]): Expr[AffineFold[S, S]]

  def sub: Expr[Getter[(Int, Int), Int]]

  def greaterThan: Expr[Getter[(Int, Int), Boolean]]

  def equal[A]: Expr[Getter[(A, A), Boolean]]

  def first[A, B]: Expr[Getter[(A, B), A]]

  def second[A, B]: Expr[Getter[(A, B), B]]

  def likeInt[S](i: Int): Expr[Getter[S, Int]]

  def likeBool[S](b: Boolean): Expr[Getter[S, Boolean]]

  def likeStr[S](s: String): Expr[Getter[S, String]]

  def id[S]: Expr[Getter[S, S]]

  def not: Expr[Getter[Boolean, Boolean]]

  def getAll[S, A](fl: Expr[Fold[S, A]]): Expr[S => List[A]]


  def lnAsGt[S, A](ln: Expr[Lens[S, A]]): Expr[Getter[S, A]]

  def gtAsFl1[S, A](gt: Expr[Getter[S, A]]): Expr[Fold1[S, A]]

  def gtAsAfl[S, A](gt: Expr[Getter[S, A]]): Expr[AffineFold[S, A]]

  def aflAsFl[S, A](afl: Expr[AffineFold[S, A]]): Expr[Fold[S, A]]

  def fl1AsFl[S, A](fl1: Expr[Fold1[S, A]]): Expr[Fold[S, A]]

  // derived methods

  def gt(i: Int): Expr[Getter[Int, Boolean]] = 
    gtVert(gtHori(id, likeInt(i)), greaterThan)

  def gtAsFl[S, A](gt: Expr[Getter[S, A]]): Expr[Fold[S, A]] =
    aflAsFl(gtAsAfl(gt))

  // def eq(i: Int): Expr[Getter[Int, Boolean]] =
  //   gtVert(gtHori(id, like(i)), equal[A])

  // def contains[S, A](fl: Expr[Fold[S, A]], a: A): Expr[Getter[S, Boolean]] =
  //   exists(fl, eq(a))

  // def all[S, A](fl: Expr[Fold[S, A]], p: Expr[Getter[A, Boolean]]) =
  //   gtVert(exists(fl, gtVert(p, not)), not)
}

object OpticLang {

  def apply[E[_]](implicit ev: OpticLang[E]): OpticLang[E] = ev

  trait AnnotatedOpticLang[Ann[_[_], _], E[_]] 
      extends OpticLang[Ann[E, ?]] with Annotated[OpticLang, Ann, E] {

    def flVert[S, A, B](
        l: Ann[E, Fold[S, A]], 
        r: Ann[E, Fold[A, B]]): Ann[E, Fold[S, B]] =
      inject(alg.flVert(run(l), run(r)))

    def flHori[S, A, B](
        l: Ann[E, Fold[S, A]], 
        r: Ann[E, Fold[S, B]]): Ann[E, Fold[S, (A, B)]] =
      inject(alg.flHori(run(l), run(r)))

    def gtVert[S, A, B](
        l: Ann[E, Getter[S, A]], 
        r: Ann[E, Getter[A, B]]): Ann[E, Getter[S, B]] =
      inject(alg.gtVert(run(l), run(r)))

    def gtHori[S, A, B](
        l: Ann[E, Getter[S, A]],
        r: Ann[E, Getter[S, B]]): Ann[E, Getter[S, (A, B)]] =
      inject(alg.gtHori(run(l), run(r)))

    def aflVert[S, A, B](
        l: Ann[E, AffineFold[S, A]], 
        r: Ann[E, AffineFold[A, B]]): Ann[E, AffineFold[S, B]] =
      inject(alg.aflVert(run(l), run(r)))

    def aflHori[S, A, B](
        l: Ann[E, AffineFold[S, A]],
        r: Ann[E, AffineFold[S, B]]): Ann[E, AffineFold[S, (A, B)]] =
      inject(alg.aflHori(run(l), run(r)))

    def filtered[S](p: Ann[E, Getter[S, Boolean]]): Ann[E, AffineFold[S, S]] =
      inject(alg.filtered(run(p)))

    def sub: Ann[E, Getter[(Int, Int), Int]] =
      inject(alg.sub)

    def greaterThan: Ann[E, Getter[(Int, Int), Boolean]] =
      inject(alg.greaterThan)

    def equal[A]: Ann[E, Getter[(A, A), Boolean]] =
      inject(alg.equal)

    def first[A, B]: Ann[E, Getter[(A, B), A]] =
      inject(alg.first)

    def second[A, B]: Ann[E, Getter[(A, B), B]] =
      inject(alg.second)

    def likeInt[S](i: Int): Ann[E, Getter[S, Int]] =
      inject(alg.likeInt(i))

    def likeBool[S](b: Boolean): Ann[E, Getter[S, Boolean]] =
      inject(alg.likeBool(b))

    def likeStr[S](s: String): Ann[E, Getter[S, String]] =
      inject(alg.likeStr(s))

    def id[S]: Ann[E, Getter[S, S]] =
      inject(alg.id)

    def not: Ann[E, Getter[Boolean, Boolean]] =
      inject(alg.not)

    def getAll[S, A](fl: Ann[E, Fold[S, A]]): Ann[E, S => List[A]] =
      inject(alg.getAll(run(fl)))

    def lnAsGt[S, A](ln: Ann[E, Lens[S, A]]): Ann[E, Getter[S, A]] =
      inject(alg.lnAsGt(run(ln)))

    def gtAsFl1[S, A](gt: Ann[E, Getter[S, A]]): Ann[E, Fold1[S, A]] =
      inject(alg.gtAsFl1(run(gt)))

    def gtAsAfl[S, A](gt: Ann[E, Getter[S, A]]): Ann[E, AffineFold[S, A]] =
      inject(alg.gtAsAfl(run(gt)))

    def aflAsFl[S, A](afl: Ann[E, AffineFold[S, A]]): Ann[E, Fold[S, A]] =
      inject(alg.aflAsFl(run(afl)))

    def fl1AsFl[S, A](fl1: Ann[E, Fold1[S, A]]): Ann[E, Fold[S, A]] =
      inject(alg.fl1AsFl(run(fl1)))
  }

  implicit val prettyPrinter = new OpticLang[Const[String, ?]] {

    def flVert[S, A, B](
        l: Const[String, Fold[S, A]], 
        r: Const[String, Fold[A, B]]): Const[String, Fold[S, B]] =
      Const(s"${l.getConst} > ${r.getConst}")

    def flHori[S, A, B](
        l: Const[String, Fold[S, A]], 
        r: Const[String, Fold[S, B]]): Const[String, Fold[S, (A, B)]] =
      Const(s"${l.getConst} * ${r.getConst}")

    def gtVert[S, A, B](
        l: Const[String, Getter[S, A]], 
        r: Const[String, Getter[A, B]]): Const[String, Getter[S, B]] =
      Const(s"${l.getConst} > ${r.getConst}")

    def gtHori[S, A, B](
        l: Const[String, Getter[S, A]],
        r: Const[String, Getter[S, B]]): Const[String, Getter[S, (A, B)]] =
      Const(s"(${l.getConst} * ${r.getConst})")

    def aflVert[S, A, B](
        l: Const[String, AffineFold[S, A]], 
        r: Const[String, AffineFold[A, B]]): Const[String, AffineFold[S, B]] =
      Const(s"${l.getConst} > ${r.getConst}")

    def aflHori[S, A, B](
        l: Const[String, AffineFold[S, A]],
        r: Const[String, AffineFold[S, B]]): Const[String, AffineFold[S, (A, B)]] =
      Const(s"${l.getConst} * ${r.getConst}")

    def filtered[S](
        p: Const[String, Getter[S, Boolean]]): Const[String, AffineFold[S, S]] =
      Const(s"filtered(${p.getConst})")

    def sub: Const[String, Getter[(Int, Int), Int]] =
      Const("sub")

    def greaterThan: Const[String, Getter[(Int, Int), Boolean]] =
      Const("greaterThan")

    def equal[A]: Const[String, Getter[(A, A), Boolean]] =
      Const("equal")

    def first[A, B]: Const[String, Getter[(A, B), A]] =
      Const("first")

    def second[A, B]: Const[String, Getter[(A, B), B]] =
      Const("second")

    def likeInt[S](i: Int): Const[String, Getter[S, Int]] =
      Const(s"likeInt(${i.toString})")

    def likeBool[S](b: Boolean): Const[String, Getter[S, Boolean]] =
      Const(s"likeBool(${b.toString})")
    
    def likeStr[S](s: String): Const[String, Getter[S, String]] =
      Const(s"""likeStr("$s")""")

    def id[S]: Const[String, Getter[S, S]] =
      Const("id")

    def not: Const[String, Getter[Boolean, Boolean]] =
      Const("not")

    def getAll[S, A](
        fl: Const[String, Fold[S, A]]): Const[String, S => List[A]] =
      Const(s"getAll(${fl.getConst})")

    def lnAsGt[S, A](
        ln: Const[String, Lens[S, A]]): Const[String, Getter[S, A]] =
      Const(s"${ln.getConst}.asGetter")

    def gtAsFl1[S, A](
        gt: Const[String, Getter[S, A]]): Const[String, Fold1[S, A]] =
      Const(s"${gt.getConst}.asFold1")

    def gtAsAfl[S, A](
        gt: Const[String, Getter[S, A]]): Const[String, AffineFold[S, A]] =
      Const(s"${gt.getConst}.asAffineFold")

    def aflAsFl[S, A](
        afl: Const[String, AffineFold[S, A]]): Const[String, Fold[S, A]] =
      Const(s"${afl.getConst}.asFold")

    def fl1AsFl[S, A](
        fl1: Const[String, Fold1[S, A]]): Const[String, Fold[S, A]] =
      Const(s"${fl1.getConst}.asFold")
  }

  type TypeNme = String
  type OpticNme = String
  
  sealed abstract class OpticKind
  case object KGetter extends OpticKind
  case object KAffineFold extends OpticKind
  case object KFold1 extends OpticKind
  case object KFold extends OpticKind

  case class TypeInfo(nme: TypeNme, isPrimitive: Boolean = false)
  case class OpticInfo(
    kind: OpticKind, 
    nme: OpticNme, 
    src: TypeInfo, 
    tgt: TypeInfo)

  type Symbol = String
  
  case class Table(src: Stream[Symbol], rows: Map[Symbol, Any])

  sealed abstract class TSemantic[E[_], A]

  case class TGetter[E[_], S, A](
    expr: TExpr[E, Getter, S, A]) extends TSemantic[E, Getter[S, A]]

  case class TAffineFold[E[_], S, A](
      expr: TExpr[E, AffineFold, S, A],
      filt: Set[TExpr[E, AffineFold, S, Boolean]] = Set.empty[TExpr[E, AffineFold, S, Boolean]]) 
    extends TSemantic[E, AffineFold[S, A]]

  case class TFold[E[_], S, A](
      expr: TExpr[E, Fold, S, A],
      filt: Set[TExpr[E, Fold, S, Boolean]] = Set.empty[TExpr[E, Fold, S, Boolean]]) 
    extends TSemantic[E, Fold[S, A]] {
    def reify(implicit ev: OpticLang[E]): E[Fold[S, A]] = ??? // reifyExpr(expr, vars)
  }

  trait OpticMap[E[_], O[_, _], O2[_, _]] {
    def apply[S, A](e: E[O[S, A]]): E[O2[S, A]]
  }

  // def reifyVV[E[_], S, A](
  //     vv: TVarVal[E, Fold, S, A],
  //     t: Table[E, Fold])(implicit 
  //     ev: OpticLang[E]): E[Fold[S, A]] = vv match {
  //   case TVarSimpleVal(w) => w.e
  //   case vnv: TVarNestedVal[E, Fold, S, A] => vnv.vs match {
  //     case ONil(v) => ev.flVert(reifyVV(t.getV(v), t), vnv.w.e)
  //     case OCons(v, vs) => 
  //       ev.flVert(reifyVV(t.getV(v), t), reifyVV(TVarNestedVal(vs, vnv.w), t))
  //   }
  // }

  sealed abstract class OList[E[_], O[_, _], S, A] {

    def mapO[O2[_, _]](f: OpticMap[E, O, O2]): OList[E, O2, S, A] = this match {
      case ONil(Var(x)) => ONil(Var(x))
      case OCons(Var(x), t) => OCons(Var(x), t.mapO(f))
    }

    def lastVar: Var[E, O, _, A] = this match {
      case ONil(x) => x
      case OCons(_, tail) => tail.lastVar
    }

    def vars: Set[String] = this match {
      case ONil(Var(x)) => Set(x)
      case OCons(Var(x), t) => t.vars + x
    }
  }

  case class ONil[E[_], O[_, _], S, A](last: Var[E, O, S, A])
    extends OList[E, O, S, A]

  case class OCons[E[_], O[_, _], S, A, B](
      head: Var[E, O, S, A], 
      tail: OList[E, O, A, B])
    extends OList[E, O, S, B]

  sealed abstract class TVarVal[E[_], O[_, _], S, A] {
    def mapO[O2[_, _]](f: OpticMap[E, O, O2]): TVarVal[E, O2, S, A] = this match {
      case TVarSimpleVal(Wrap(e, info)) => TVarSimpleVal(Wrap(f(e), info))
      case vnv: TVarNestedVal[E, O, S, A] =>  
        TVarNestedVal(vnv.vs.mapO(f), Wrap(f(vnv.w.e), vnv.w.info))
    }
  }

  case class TVarSimpleVal[E[_], O[_, _], S, A](w: Wrap[E, O, S, A])
    extends TVarVal[E, O, S, A]

  trait TVarNestedVal[E[_], O[_, _], S, B] extends TVarVal[E, O, S, B] {
    type A
    def vs: OList[E, O, S, A]
    def w: Wrap[E, O, A, B]
    override def equals(other: Any): Boolean = other match {
      case vnv: TVarNestedVal[E, O, S, _] => vs == vnv.vs && vnv.w == w
      case _ => false
    }
  }

  object TVarNestedVal {

    type Aux[E[_], O[_, _], S, A2, B] = 
      TVarNestedVal[E, O, S, B] { type A = A2 }

    def apply[E[_], O[_, _], S, A2, B](
        vs2: OList[E, O, S, A2], 
        w2: Wrap[E, O, A2, B]): Aux[E, O, S, A2, B] = 
      new TVarNestedVal[E, O, S, B] {
        type A = A2
        val vs = vs2
        val w = w2
      }
  }

  // def reifyExpr[E[_], S, A](
  //     expr: TExpr[E, Fold, S, A],
  //     t: Table[E, Fold])(implicit
  //     ev: OpticLang[E]): E[Fold[S, A]] = expr match {
  //   case Product(l, r, is) => ???
  //     // is.subst[λ[x => E[Fold[S, x]]]](ev.flHori(reifyExpr(l, lt), reifyExpr(r, rt)))
  //   case Vertical(u, d) => ??? // ev.flVert(reifyExpr(u, ut), reifyExpr(d, dt))
  //   case x: Var[E, Fold, S, A] => reifyVV(t.getV(x), t)
  //   case w: Wrap[E, Fold, S, A] => w.e
  //   case LikeInt(i, is) => 
  //     is.flip.subst[λ[x => E[Fold[S, x]]]](ev.gtAsFl(ev.likeInt[S](i)))
  //   case LikeBool(b, is) =>
  //     is.flip.subst[λ[x => E[Fold[S, x]]]](ev.gtAsFl(ev.likeBool[S](b)))
  //   case Not(is1, is2) => 
  //     is2.flip.subst[λ[x => E[Fold[S, x]]]](
  //       is1.flip.subst[λ[x => E[Fold[x, Boolean]]]](ev.gtAsFl(ev.not)))
  //   case Sub(is1, is2) =>
  //     is2.flip.subst[λ[x => E[Fold[S, x]]]](
  //       is1.flip.subst[λ[x => E[Fold[x, Int]]]](ev.gtAsFl(ev.sub)))
  // }

  sealed abstract class TExpr[E[_], O[_, _], S, A] {

    def mapO[O2[_, _]](f: OpticMap[E, O, O2]): TExpr[E, O2, S, A] = this match {
      case Product(l, r, is) => Product(l.mapO(f), r.mapO(f), is)
      case Vertical(u, d) => Vertical(u.mapO(f), d.mapO(f))
      case LikeInt(i, is) => LikeInt(i, is)
      case LikeBool(b, is) => LikeBool(b, is)
      case x: TSel[E, O, S, A] => x.mapOSel(f)
    }

    def vars: Set[String] = this match {
      case Product(l, r, _) => l.vars ++ r.vars
      case Vertical(u, d) => u.vars ++ d.vars
      case Var(name) => Set(name)
      case _ => Set.empty
    }
  }
  
  object TExpr {

    def product[E[_], O[_, _], S, A, B, C](
        l: TExpr[E, O, S, A],
        r: TExpr[E, O, S, B],
        is: (A, B) === C): TExpr[E, O, S, C] =
      Product(l, r, is)
    
    def vertical[E[_], O[_, _], S, A, B](
        u: TExpr[E, O, S, A],
        d: TExpr[E, O, A, B]): TExpr[E, O, S, B] =
      Vertical(u, d)

    def likeInt[E[_], O[_, _], S, A](
        i: Int,
        is: A === Int): TExpr[E, O, S, A] =
      LikeInt(i, is)

    def likeBool[E[_], O[_, _], S, A](
        b: Boolean,
        is: A === Boolean): TExpr[E, O, S, A] =
      LikeBool(b, is)
  }
  
  case class Product[E[_], O[_, _], S, A, B, C](
    l: TExpr[E, O, S, A],
    r: TExpr[E, O, S, B],
    is: (A, B) === C) extends TExpr[E, O, S, C]

  case class Vertical[E[_], O[_, _], S, A, B](
    u: TExpr[E, O, S, A],
    d: TExpr[E, O, A, B]) extends TExpr[E, O, S, B]

  case class LikeInt[E[_], O[_, _], S, A](
    i: Int,
    is: A === Int) extends TExpr[E, O, S, A]

  case class LikeBool[E[_], O[_, _], S, A](
    b: Boolean,
    is: A === Boolean) extends TExpr[E, O, S, A]

  sealed abstract class TSel[E[_], O[_, _], S, A] extends TExpr[E, O, S, A] {
    def mapOSel[O2[_, _]](f: OpticMap[E, O, O2]): TSel[E, O2, S, A] = this match {
      case Var(name) => Var(name)
      case Wrap(e, inf) => Wrap(f(e), inf)
      case Sub(is1, is2) => Sub(is1, is2)
      case Gt(is1, is2) => Gt(is1, is2)
      case Not(is1, is2) => Not(is1, is2)
    }
  }

  case class Var[E[_], O[_, _], S, A](
    name: String) extends TSel[E, O, S, A]

  case class Wrap[E[_], O[_, _], S, A](
    e: E[O[S, A]],
    info: OpticInfo) extends TSel[E, O, S, A]

  case class Sub[E[_], O[_, _], S, A](
    is1: S === (Int, Int),
    is2: A === Int) extends TSel[E, O, S, A]

  case class Gt[E[_], O[_, _], S, A](
    is1: S === (Int, Int),
    is2: A === Boolean) extends TSel[E, O, S, A]

  case class Not[E[_], O[_, _], S, A](
    is1: S === Boolean,
    is2: A === Boolean) extends TSel[E, O, S, A]

  // XXX: we need a safer table implementation, since this is a really ugly
  // workaround, but this is good enough for the time being.
  implicit class TableOps(table: Table) {

    private def splitTables = table.rows.partition { 
      case (_, TVarSimpleVal(_)) => true 
      case _ => false 
    }

    def simpleTable[E[_], O[_, _]]: Map[String, TVarSimpleVal[E, O, _, _]] =
      splitTables._1.asInstanceOf[Map[String, TVarSimpleVal[E, O, _, _]]]

    def nestedTable[E[_], O[_, _]]: Map[String, TVarNestedVal[E, O, _, _]] =
      splitTables._2.asInstanceOf[Map[String, TVarNestedVal[E, O, _, _]]]
  }

  type Semantic[E[_], A] = State[Table, TSemantic[E, A]]

  import State._

  implicit def tsemantic[E[_]: OpticLang]: OpticLang[Semantic[E, ?]] = 
      new OpticLang[Semantic[E, ?]] {

    def gtVertAux[S, A, B](
        u: TExpr[E, Getter, S, A], 
        d: TExpr[E, Getter, A, B]): State[Table, TExpr[E, Getter, S, B]] =
      (u, d) match {
        case (_, Product(l, r, is)) =>
          (gtVertAux(u, l) |@| gtVertAux(u, r)) { (l1, r1) => 
            TExpr.product(l1, r1, is)
          }
        case (_, Vertical(u1, d1)) => 
          gtVertAux(u, u1) >>= (u2 => gtVertAux(u2, d1))
        case (Product(l, _, _), Wrap(_, inf)) if inf.nme == "first" => 
          l.asInstanceOf[TExpr[E, Getter, S, B]].point[State[Table, ?]]
        case (Product(_, r, _), Wrap(_, inf)) if inf.nme == "second" =>
          r.asInstanceOf[TExpr[E, Getter, S, B]].point[State[Table, ?]]
        case (Wrap(_, inf), e) if inf.nme == "id" => 
          e.asInstanceOf[TExpr[E, Getter, S, B]].point[State[Table, ?]]
        case (e, Wrap(_, inf)) if inf.nme == "id" => 
          e.asInstanceOf[TExpr[E, Getter, S, B]].point[State[Table, ?]]
        case (_, LikeInt(i, is)) => 
          TExpr.likeInt(i, is).point[State[Table, ?]]
        case (_, LikeBool(b, is)) => 
          TExpr.likeBool(b, is).point[State[Table, ?]]
        case (Product(l, LikeInt(0, _), _), Sub(_, _)) => 
          l.asInstanceOf.point[State[Table, ?]]
        case (Product(LikeInt(x, _), LikeInt(y, _), _), Sub(_, is)) =>
          TExpr.likeInt(x - y, is).point[State[Table, ?]]
        case (LikeBool(b, _), Not(_, is)) =>
          TExpr.likeBool(! b, is).point[State[Table, ?]]
        case (Vertical(e, Not(is1, _)), Not(_, is2)) =>
          is2.flip.subst(is1.subst(e)).point[State[Table, ?]]
        case (Not(is1, _), Not(_, is2)) => {
          for {
            tmp <- id[Boolean]
            TGetter(expr) = tmp
          } yield is1.flip.subst[λ[x => TExpr[E, Getter, x, B]]](
                    is2.flip.subst[λ[x => TExpr[E, Getter, Boolean, x]]](expr))
        }
        case (x@Var(_), y@Var(s)) =>
          for {
            yv <- getVal(y)
            e = yv match {
              case TVarSimpleVal(w) => TVarNestedVal(ONil(x), w)
              case vnv: TVarNestedVal[E, Getter, _, _] => 
                TVarNestedVal(OCons(x, vnv.vs), vnv.w)
            }
            z <- assignVal(e) 
          } yield z
        case (Vertical(prev, x@Var(_)), y@Var(s)) =>
          gtVertAux(x, y) >>= (z => gtVertAux(prev, z))
        case _ => TExpr.vertical(u, d).point[State[Table, ?]]
      }

    def gtVert[S, A, B](
        usem: Semantic[E, Getter[S, A]], 
        dsem: Semantic[E, Getter[A, B]]): Semantic[E, Getter[S, B]] =
      for {
        tmp <- usem
        TGetter(expr1) = tmp
        tmp <- dsem
        TGetter(expr2) = tmp
        expr3 <- gtVertAux(expr1, expr2)
      } yield TGetter(expr3)

    def gtHori[S, A, B](
        l: Semantic[E, Getter[S, A]],
        r: Semantic[E, Getter[S, B]]): Semantic[E, Getter[S, (A, B)]] =
      for {
        tmp <- l
        TGetter(expr1) = tmp
        tmp <- r
        TGetter(expr2) = tmp
      } yield TGetter(Product(expr1, expr2, Leibniz.refl[(A, B)]))

    def aflVertAux[S, A, B](
        u: TExpr[E, AffineFold, S, A], 
        d: TExpr[E, AffineFold, A, B]): State[Table, TExpr[E, AffineFold, S, B]] =
      (u, d) match {
        case (_, Product(l, r, is)) =>
          (aflVertAux(u, l) |@| aflVertAux(u, r)) { (l1, r1) => 
            TExpr.product(l1, r1, is)
          }
        case (_, Vertical(u1, d1)) => 
          aflVertAux(u, u1) >>= (u2 => aflVertAux(u2, d1))
        case (Product(l, _, _), Wrap(_, inf)) if inf.nme == "first" => 
          l.asInstanceOf[TExpr[E, AffineFold, S, B]].point[State[Table, ?]]
        case (Product(_, r, _), Wrap(_, inf)) if inf.nme == "second" =>
          r.asInstanceOf[TExpr[E, AffineFold, S, B]].point[State[Table, ?]]
        case (Wrap(_, inf), e) if inf.nme == "id" => 
          e.asInstanceOf[TExpr[E, AffineFold, S, B]].point[State[Table, ?]]
        case (e, Wrap(_, inf)) if inf.nme == "id" => 
          e.asInstanceOf[TExpr[E, AffineFold, S, B]].point[State[Table, ?]]
        case (_, LikeInt(i, is)) => 
          TExpr.likeInt(i, is).point[State[Table, ?]]
        case (_, LikeBool(b, is)) => 
          TExpr.likeBool(b, is).point[State[Table, ?]]
        case (Product(l, LikeInt(0, _), _), Sub(_, _)) => 
          l.asInstanceOf.point[State[Table, ?]]
        case (Product(LikeInt(x, _), LikeInt(y, _), _), Sub(_, is)) =>
          TExpr.likeInt(x - y, is).point[State[Table, ?]]
        case (LikeBool(b, _), Not(_, is)) =>
          TExpr.likeBool(! b, is).point[State[Table, ?]]
        case (Vertical(e, Not(is1, _)), Not(_, is2)) =>
          is2.flip.subst(is1.subst(e)).point[State[Table, ?]]
        case (Not(is1, _), Not(_, is2)) => {
          for {
            tmp <- gtAsAfl(id[Boolean])
            TAffineFold(expr, _) = tmp
          } yield is1.flip.subst[λ[x => TExpr[E, AffineFold, x, B]]](
                    is2.flip.subst[λ[x => TExpr[E, AffineFold, Boolean, x]]](expr))
        }
        case (x@Var(_), y@Var(s)) =>
          for {
            yv <- getVal(y)
            e = yv match {
              case TVarSimpleVal(w) => TVarNestedVal(ONil(x), w)
              case vnv: TVarNestedVal[E, AffineFold, _, _] => 
                TVarNestedVal(OCons(x, vnv.vs), vnv.w)
            }
            z <- assignVal(e) 
          } yield z
        case (Vertical(prev, x@Var(_)), y@Var(s)) =>
          aflVertAux(x, y) >>= (z => aflVertAux(prev, z))
        case _ => TExpr.vertical(u, d).point[State[Table, ?]]
      }

    def aflVert[S, A, B](
        u: Semantic[E, AffineFold[S, A]], 
        d: Semantic[E, AffineFold[A, B]]): Semantic[E, AffineFold[S, B]] =
      for {
        tmp <- u
        TAffineFold(expr1, filt1) = tmp
        tmp <- d
        TAffineFold(expr2, filt2) = tmp
        expr3 <- aflVertAux(expr1, expr2)
        filt3 <- filt2.toList.traverse(aflVertAux(expr1, _))
      } yield TAffineFold(expr3, filt1 ++ filt3)

    def aflHori[S, A, B](
        l: Semantic[E, AffineFold[S, A]],
        r: Semantic[E, AffineFold[S, B]]) =
      for {
        tmp <- l
        TAffineFold(expr1, filt1) = tmp
        tmp <- r
        TAffineFold(expr2, filt2) = tmp
      } yield TAffineFold(
          Product(expr1, expr2, Leibniz.refl[(A, B)]), 
          filt1 ++ filt2)

    def flVertAux[S, A, B](
        u: TExpr[E, Fold, S, A], 
        d: TExpr[E, Fold, A, B]): State[Table, TExpr[E, Fold, S, B]] =
      (u, d) match {
        case (_, Product(l, r, is)) =>
          (flVertAux(u, l) |@| flVertAux(u, r)) { (l1, r1) => 
            TExpr.product(l1, r1, is)
          }
        case (_, Vertical(u1, d1)) => 
          flVertAux(u, u1) >>= (u2 => flVertAux(u2, d1))
        case (Product(l, _, _), Wrap(_, inf)) if inf.nme == "first" => 
          l.asInstanceOf[TExpr[E, Fold, S, B]].point[State[Table, ?]]
        case (Product(_, r, _), Wrap(_, inf)) if inf.nme == "second" =>
          r.asInstanceOf[TExpr[E, Fold, S, B]].point[State[Table, ?]]
        case (Wrap(_, inf), e) if inf.nme == "id" => 
          e.asInstanceOf[TExpr[E, Fold, S, B]].point[State[Table, ?]]
        case (e, Wrap(_, inf)) if inf.nme == "id" => 
          e.asInstanceOf[TExpr[E, Fold, S, B]].point[State[Table, ?]]
        case (_, LikeInt(i, is)) => 
          TExpr.likeInt(i, is).point[State[Table, ?]]
        case (_, LikeBool(b, is)) => 
          TExpr.likeBool(b, is).point[State[Table, ?]]
        case (Product(l, LikeInt(0, _), _), Sub(_, _)) => 
          l.asInstanceOf.point[State[Table, ?]]
        case (Product(LikeInt(x, _), LikeInt(y, _), _), Sub(_, is)) =>
          TExpr.likeInt(x - y, is).point[State[Table, ?]]
        case (LikeBool(b, _), Not(_, is)) =>
          TExpr.likeBool(! b, is).point[State[Table, ?]]
        case (Vertical(e, Not(is1, _)), Not(_, is2)) =>
          is2.flip.subst(is1.subst(e)).point[State[Table, ?]]
        case (Not(is1, _), Not(_, is2)) => {
          for {
            tmp <- aflAsFl(gtAsAfl(id[Boolean]))
            TFold(expr, _) = tmp
          } yield is1.flip.subst[λ[x => TExpr[E, Fold, x, B]]](
                    is2.flip.subst[λ[x => TExpr[E, Fold, Boolean, x]]](expr))
        }
        case (x@Var(_), y@Var(s)) =>
          for {
            yv <- getVal(y)
            e = yv match {
              case TVarSimpleVal(w) => TVarNestedVal(ONil(x), w)
              case vnv: TVarNestedVal[E, Fold, _, _] => 
                TVarNestedVal(OCons(x, vnv.vs), vnv.w)
            }
            z <- assignVal(e) 
          } yield z
        case (Vertical(prev, x@Var(_)), y@Var(s)) =>
          flVertAux(x, y) >>= (z => flVertAux(prev, z))
        case _ => TExpr.vertical(u, d).point[State[Table, ?]]
      }

    def flVert[S, A, B](
        u: Semantic[E, Fold[S, A]], 
        d: Semantic[E, Fold[A, B]]) =
      for {
        tmp <- u
        TFold(expr1, filt1) = tmp
        tmp <- d
        TFold(expr2, filt2) = tmp
        expr3 <- flVertAux(expr1, expr2)
        filt3 <- filt2.toList.traverse(flVertAux(expr1, _))
      } yield TFold(expr3, filt1 ++ filt3)

    def flHori[S, A, B](
        l: Semantic[E, Fold[S, A]], 
        r: Semantic[E, Fold[S, B]]): Semantic[E, Fold[S, (A, B)]] =
      for {
        tmp <- l
        TFold(expr1, filt1) = tmp
        tmp <- r
        TFold(expr2, filt2) = tmp
      } yield TFold(
          Product(expr1, expr2, Leibniz.refl[(A, B)]), 
          filt1 ++ filt2)

    def filtered[S](
        p: Semantic[E, Getter[S, Boolean]]): Semantic[E, AffineFold[S, S]] = 
      for {
        tmp <- gtAsAfl(p)
        TAffineFold(fil, _) = tmp
        tmp <- gtAsAfl(id[S])
        TAffineFold(exp, _) = tmp
      } yield TAffineFold(expr = exp, filt = Set(fil))

    def sub: Semantic[E, Getter[(Int, Int), Int]] =
      state(TGetter(expr = Sub(implicitly, implicitly)))

    def greaterThan: Semantic[E, Getter[(Int, Int), Boolean]] =
      state(TGetter(expr = Gt(implicitly, implicitly)))

    def equal[A]: Semantic[E, Getter[(A, A), Boolean]] = ???

    def first[A, B]: Semantic[E, Getter[(A, B), A]] =
      state(TGetter(expr = Wrap(OpticLang[E].first,
        OpticInfo(KGetter, "first", TypeInfo("(A, B)"), TypeInfo("A")))))

    def second[A, B]: Semantic[E, Getter[(A, B), B]] =
      state(TGetter(expr = Wrap(OpticLang[E].second,
        OpticInfo(KGetter, "second", TypeInfo("(A, B)"), TypeInfo("B")))))

    def likeInt[S](i: Int): Semantic[E, Getter[S, Int]] =
      state(TGetter(expr = LikeInt(i, implicitly)))

    def likeBool[S](b: Boolean): Semantic[E, Getter[S, Boolean]] =
      state(TGetter(expr = LikeBool(b, implicitly)))

    def likeStr[S](s: String): Semantic[E, Getter[S, String]] =
      ???

    def id[S]: Semantic[E, Getter[S, S]] =
      state(TGetter(expr = Wrap(OpticLang[E].id,
        OpticInfo(KGetter, "id", TypeInfo("S"), TypeInfo("S")))))

    def not: Semantic[E, Getter[Boolean, Boolean]] =
      ???

    def getAll[S, A](fl: Semantic[E, Fold[S, A]]): Semantic[E, S => List[A]] = ???

    def lnAsGt[S, A](ln: Semantic[E, Lens[S, A]]): Semantic[E, Getter[S, A]] = ???

    def gtAsFl1[S, A](gt: Semantic[E, Getter[S, A]]): Semantic[E, Fold1[S, A]] = ???

    def gtAsAfl[S, A](
        gt: Semantic[E, Getter[S, A]]): Semantic[E, AffineFold[S, A]] =
      for {
        tmp <- gt
        TGetter(expr) = tmp
        f = new OpticMap[E, Getter, AffineFold] {
          def apply[X, Y](gt: E[Getter[X, Y]]) = OpticLang[E].gtAsAfl(gt)
        }
      } yield TAffineFold(expr.mapO(f))

    def aflAsFl[S, A](
        afl: Semantic[E, AffineFold[S, A]]): Semantic[E, Fold[S, A]] =
      for {
        tmp <- afl
        TAffineFold(expr, filt) = tmp
        f = new OpticMap[E, AffineFold, Fold] {
          def apply[X, Y](afl: E[AffineFold[X, Y]]) = OpticLang[E].aflAsFl(afl)
        }
      } yield TFold(expr.mapO(f), filt.map(_.mapO(f)))

    def fl1AsFl[S, A](fl1: Semantic[E, Fold1[S, A]]): Semantic[E, Fold[S, A]] = ???

    private def fresh: State[Table, String] =
      for {
        s <- gets[Table, Stream[String]](_.src)
        _ <- modify[Table](_.copy(src = s.tail))
      } yield s.head

    def assignVal[O[_, _], S, A](
        vv: TVarVal[E, O, S, A]): State[Table, Var[E, O, S, A]] =
      for {
        os <- gets[Table, Option[String]](_.rows.find(_._2 == vv).map(_._1))
        s <- os.fold(
          fresh >>! (s => modify[Table](t => t.copy(rows = t.rows + (s -> vv)))))(
          _.point[State[Table, ?]])
      } yield Var(s)

    private def getVal[O[_, _], S, A](
        v: Var[E, O, S, A]): State[Table, TVarVal[E, O, S, A]] =
      gets[Table, TVarVal[E, O, S, A]](
        _.rows(v.name).asInstanceOf[TVarVal[E, O, S, A]])

    private def varDeps(s: String): State[Table, Set[String]] =
      gets[Table, Set[String]] { t =>
        if (t.rows.get(s) == None) {
          println()
          println(s"Asking for ${s}")
          println(t.rows)
          println()
        }
        t.rows(s) match {
          case vv: TVarNestedVal[_, _, _, _] => vv.vs.vars + s
          case _ => Set(s)
        }
      }

    private def removeVars(vars: Set[String]): State[Table, Unit] =
      for {
        vars2 <- vars.toList.traverse(varDeps).map(_.flatten)
        _ <- modify[Table](t => t.copy(rows = t.rows.filterKeys(vars2.contains(_))))
      } yield ()
  }

  trait Syntax {

    implicit class FoldOps[Expr[_], S, A](
        l: Expr[Fold[S, A]])(implicit 
        O: OpticLang[Expr]) {
      def >[B](r: Expr[Fold[A, B]]): Expr[Fold[S, B]] = O.flVert(l, r)
      def *[B](r: Expr[Fold[S, B]]): Expr[Fold[S, (A, B)]] = O.flHori(l, r)
    }

    implicit class Fold1Ops[Expr[_], S, A](
        l: Expr[Fold1[S, A]])(implicit
        O: OpticLang[Expr]) {
      def asFold: Expr[Fold[S, A]] = O.fl1AsFl(l)
    }

    implicit class AffineFoldOps[Expr[_], S, A](
        l: Expr[AffineFold[S, A]])(implicit
        O: OpticLang[Expr]) {
      def asFold: Expr[Fold[S, A]] = O.aflAsFl(l)
      def >[B](r: Expr[AffineFold[A, B]]): Expr[AffineFold[S, B]] = 
        O.aflVert(l, r)
      def *[B](r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, (A, B)]] = 
        O.aflHori(l, r)
      def <*[B](r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, A]] = 
        O.aflVert(O.aflHori(l, r), O.gtAsAfl(O.first[A, B]))
      def *>[B](r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, B]] = 
        O.aflVert(O.aflHori(l, r), O.gtAsAfl(O.second[A, B]))
    }

    implicit class GetterOps[Expr[_], S, A](
        l: Expr[Getter[S, A]])(implicit
        O: OpticLang[Expr]) {
      def asFold1: Expr[Fold1[S, A]] = O.gtAsFl1(l)
      def asAffineFold: Expr[AffineFold[S, A]] = O.gtAsAfl(l)
      def asFold: Expr[Fold[S, A]] = O.aflAsFl(O.gtAsAfl(l))
      def >[B](r: Expr[Getter[A, B]]): Expr[Getter[S, B]] = O.gtVert(l, r)
      def *[B](r: Expr[Getter[S, B]]): Expr[Getter[S, (A, B)]] = O.gtHori(l, r)
    }

    implicit class IntGetterOps[Expr[_], S](
        l: Expr[Getter[S, Int]])(implicit
        O: OpticLang[Expr]) {
      def -(r: Expr[Getter[S, Int]]): Expr[Getter[S, Int]] =
        O.gtVert(O.gtHori(l, r), O.sub)
    }
  }

  object syntax extends Syntax
}

