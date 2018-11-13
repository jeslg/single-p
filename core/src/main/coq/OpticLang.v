Require Import Program.Basics.
Require Import Coq.Strings.String.
Require Import Coq.Init.Specif.
Require Import Coq.Lists.List.
Require Import Coq.Vectors.VectorDef.
Require Import Coq.Arith.PeanoNat.
Require Import Coq.Init.Logic.
Require Import Coq.Bool.Bool.
Require Import Coq.Logic.FunctionalExtensionality.

Open Scope program_scope.

Generalizable All Variables.

(* koky *)

Definition fork {S A B} (f : S -> A) (g : S -> B) : S -> A * B :=
  fun s => (f s, g s).

Inductive listCart (A : Type) : Type :=
| wrap : list A -> listCart A.

Arguments wrap [A].

Definition unwrap {A} (w : listCart A) : list A :=
  match w with | wrap xs => xs end.

Inductive boolOr : Type :=
| wrapBool : bool -> boolOr.

Definition unwrapBool (bo : boolOr) : bool :=
  match bo with | wrapBool b => b end.

Class Eq (A : Type) :=
{ eqb : A -> A -> bool }.

Notation "a1 == a2" := (eqb a1 a2) (at level 40, left associativity).

Instance stringEq : Eq string :=
{ eqb s t := if string_dec s t then true else false }.

Class Semigroup (M : Type) :=
{ mappend : M -> M -> M }.

Class Monoid (M : Type) `{Semigroup M} :=
{ mempty : M }.

Instance boolSemigroup : Semigroup bool :=
{ mappend m1 m2 := m1 && m2 }.

Instance boolMonoid : Monoid bool :=
{ mempty := true }.

Instance boolOrSemigroup : Semigroup boolOr :=
{ mappend m1 m2 := wrapBool (unwrapBool m1 || unwrapBool m2) }.

Instance boolOrMonoid : Monoid boolOr :=
{ mempty := wrapBool false }.

Instance listSemigroup {A : Type} : Semigroup (list A) :=
{ mappend m1 m2 := m1 ++ m2 }.

Instance listMonoid {A : Type} : Monoid (list A) :=
{ mempty := List.nil }.

Instance listCartSemigroup {A : Type} : Semigroup (listCart A) :=
{ mappend m1 m2 := wrap (mappend (unwrap m1) (unwrap m2)) }.

Instance listCartMonoid {A : Type} : Monoid (listCart A) :=
{ mempty := wrap (List.nil) }.

Class Foldable (F : Type -> Type) :=
{ fold : forall {A} `{Monoid M}, (A -> M) -> F A -> M }.

Instance listFoldable : Foldable list :=
{ fold := fun _ _ _ _ f => List.fold_right (mappend ∘ f) mempty }.

Definition option_fold {A B} (some : A -> B) (none : B) (oa : option A) : B :=
  match oa with
  | Some a => some a
  | None => none
end.

Instance optionFoldable : Foldable option :=
{ fold := fun _ _ _ _ f s => option_fold f mempty s}.

Inductive nel (A : Type) : Type :=
| nel_nil : A -> nel A
| nel_cons : A -> nel A -> nel A.

Arguments nel_nil [A].
Arguments nel_cons [A].

Fixpoint nel_fold {A B} (f : A -> B -> B) (g : A -> B) (xs : nel A) : B :=
  match xs with
  | nel_nil a => g a
  | nel_cons a xs' => f a (nel_fold f g xs')
  end.

Fixpoint nel_combine {A B} (xs : nel A) (ys : nel B) : nel (A * B) :=
  match (xs, ys) with
  | (nel_nil a, nel_nil b) => nel_nil (a,  b)
  | (nel_nil a, nel_cons b _) => nel_nil (a, b)
  | (nel_cons a _, nel_nil b) => nel_nil (a, b)
  | (nel_cons a xs', nel_cons b ys') => nel_cons (a, b) (nel_combine xs' ys')
  end.

Fixpoint nel_cat {A} (xs : nel A) (ys : nel A) : nel A :=
  nel_fold (fun a b => nel_cons a b) (fun a => nel_cons a ys) xs.

Fixpoint vec1ToNelAux {A n} (v : t A n) (prev : A) : nel A :=
  match v with
  | nil _ => nel_nil prev
  | cons _ nprev _ v2 => nel_cons prev (vec1ToNelAux v2 nprev)
  end.

Definition vec1ToNel {A n} (v : t A (S n)) : nel A :=
  vec1ToNelAux (tl v) (hd v).

Instance nelSemigroup {A : Type} : Semigroup (nel A) :=
{ mappend m1 m2 := nel_cat m1 m2 }.

Class Foldable1 (F : Type -> Type) :=
{ fold1 : forall {A} `{Semigroup M}, (A -> M) -> F A -> M }.

Instance nelFoldable1 : Foldable1 nel :=
{ fold1 := fun _ _ _ f s => nel_fold (mappend ∘ f) f s }.

Class Applicative (M : Type -> Type) : Type :=
{ pure : forall {A}, A -> M A
; ap : forall {A B}, M A -> M (A -> B) -> M B
; tupled {A B} (ma : M A) (mb : M B) : M (prod A B) :=
    ap mb (ap ma (pure pair))
}.

Instance listApplicative : Applicative list :=
{ pure := fun _ a => List.cons a List.nil
; ap := fun _ _ la lf => 
    List.map (fun fa => match fa with | (a, f) => f a end) (List.combine la lf)
}.

Instance listCartFoldable : Foldable listCart :=
{ fold := fun _ _ _ _ f s => fold f (unwrap s) }.

Instance listCartApplicative : Applicative listCart :=
{ pure := fun _ a => wrap (pure a)
; ap := fun A B la lf =>
    let f := fun pair => match pair with | (a, f) => f a end in
    wrap (List.map f (list_prod (unwrap la) (unwrap lf)))
}.

Instance optionApplicative : Applicative option :=
{ pure := fun _ => Some
; ap := fun _ _ oa og => match (oa, og) with
    | (Some a, Some g) => Some (g a)
    | _ => None
    end
}.

Class Monad (M : Type -> Type) `{Applicative M} : Type :=
{ bind : forall {A B}, M A -> (A -> M B) -> M B
}.

Notation "ma >>= f" := (bind ma f) (at level 40, left associativity).

Instance optionMonad : Monad option :=
{ bind := fun _ _ oa f => match oa with | Some a => f a | None => None end 
}.

(****************)
(* Plain optics *)
(****************)

(* FOLD *)

Record Fold (S A : Type) := mkFold
{ foldMap `{Monoid M} : (A -> M) -> S -> M }.

Arguments mkFold [S A].
Arguments foldMap [S A].

Definition flVerCompose {S A B} (fl1 : Fold S A) (fl2 : Fold A B) : Fold S B :=
  mkFold (fun _ _ _ f s => foldMap fl1 _ _ _ (foldMap fl2 _ _ _ f) s).

Definition flGenCompose {S A B} F 
    `{Applicative F, Foldable F, Monoid (F A), Monoid (F B)}
    (fl1 : Fold S A) (fl2 : Fold S B) : Fold S (A * B) :=
  mkFold (fun _ _ _ f s => @fold F _ _ _ _ _ f (tupled
    (foldMap fl1 _ _ _ pure s)
    (foldMap fl2 _ _ _ pure s))).

Definition flProCompose {S A B}
    (fl1 : Fold S A) (fl2 : Fold S B) : Fold S (A * B) :=
  flGenCompose listCart fl1 fl2.

Definition flHorCompose {S A B}
    (fl1 : Fold S A) (fl2 : Fold S B) : Fold S (A * B) :=
  flGenCompose list fl1 fl2.

Definition idFl {S : Type} : Fold S S :=
  mkFold (fun _ _ _ f s => f s).

(* SETTER *)

Record Setter (S A : Type) := mkSetter
{ modify : (A -> A) -> S -> S
}.

Arguments mkSetter [S A].
Arguments modify [S A].

Definition stVerCompose {S A B}
    (st1 : Setter S A) (st2: Setter A B) : Setter S B :=
  mkSetter (fun f => modify st1 (modify st2 f)).

(* I wasn't able to implement horizontal composition for Setters! *)

Definition idSt {S : Type} : Setter S S :=
  mkSetter id.

(* FOLD1 *)

Record Fold1 S A := mkFold1
{ foldMap1 `{Semigroup M} : (A -> M) -> S -> M }.

Arguments mkFold1 [S A].
Arguments foldMap1 [S A].

Definition fl1VerCompose {S A B} 
    (fl1 : Fold1 S A) (fl2 : Fold1 A B) : Fold1 S B :=
  mkFold1 (fun _ _ f => foldMap1 fl1 _ _ (foldMap1 fl2 _ _ f)).

Definition fl1ProCompose {S A B}
    (fl1 : Fold1 S A) (fl2 : Fold1 S B) : Fold1 S (A * B) :=
  mkFold1 (fun _ _ f s => foldMap1 fl1 _ _ (fun a => 
    foldMap1 fl2 _ _ (fun b => f (a, b)) s) s).

Definition fl1HorCompose {S A B}
    (fl1 : Fold1 S A) (fl2 : Fold1 S B) : Fold1 S (A * B) :=
  mkFold1 (fun M m f s => fold1 f (nel_combine
    (foldMap1 fl1 _ _ (fun a => nel_nil a) s)
    (foldMap1 fl2 _ _ (fun b => nel_nil b) s))).

Definition fl1AsFold {S A} (fl : Fold1 S A) : Fold S A :=
  mkFold (fun _ _ _ => foldMap1 fl _ _).

Definition idFl1 {S : Type} : Fold1 S S :=
  mkFold1 (fun _ _ => id).

(* TRAVERSAL *)

Definition result S A (n : nat) : Type := 
  t A n * (t A n -> S).

Definition nResult {S A} (sig : sigT (result S A)) : nat :=
  match sig with | existT _ n _ => n end.

Record Traversal S A := mkTraversal
{ extract : S -> sigT (result S A) }.

Arguments mkTraversal [S A].
Arguments extract [S A].

(* TODO: Combinators for this Traversal representation aren't trivial at all! *)

Definition trVerCompose {S A B}
    (tr1 : Traversal S A) (tr2 : Traversal A B) : Traversal S B.
Proof. Admitted.

(* XXX: I was not able to prove this statement, but I think it makes a lot of 
   sense. Broadly, I think that you can't provide a cartesian combinator for 
   traversals, but you can provide the horizontal (zip) one, as long as both 
   traversals have the very same number of foci. Only in this case it's feasible 
   to implement putAll. *)
Definition trHorCompose {S A B}
    (tr1 : Traversal S A) (tr2 : Traversal S B)
    (pro : forall s, nResult (extract tr1 s) = nResult (extract tr2 s))
    : Traversal S (A * B).
Proof. Admitted.

Definition idTr {S : Type} : Traversal S S :=
  mkTraversal (fun s => existT _ 1 (cons S s 0 (nil S), hd)).

Definition each {S : Type} : Traversal (list S) S :=
  mkTraversal (fun xs => existT _ (length xs) (of_list xs, to_list)).

Definition trAsSetter {S A} (tr : Traversal S A) : Setter S A.
Proof. Admitted.

Definition trAsFold {S A} (tr : Traversal S A) : Fold S A :=
  mkFold (fun _ _ _ f s =>
    match extract tr s with | existT _ _ (v, _) => fold f (to_list v) end).

(* AFFINE FOLD *)

Record AffineFold S A := mkAffineFold
{ afold : S -> option A }.

Arguments mkAffineFold [S A].
Arguments afold [S A].

Definition aflVerCompose {S A B}
    (af1 : AffineFold S A) (af2 : AffineFold A B) : AffineFold S B :=
  mkAffineFold (fun s => afold af1 s >>= (fun a => afold af2 a)).

(* Both product and horizontal are equivalent *)

Definition aflHorCompose {S A B}
    (af1 : AffineFold S A) (af2 : AffineFold S B) : AffineFold S (A * B) :=
  mkAffineFold (fun s => tupled (afold af1 s) (afold af2 s)).

Definition aflAsFold {S A} (afl : AffineFold S A) : Fold S A :=
  mkFold (fun _ _ _ f s => fold f (afold afl s)).

Definition idAfl {S} : AffineFold S S :=
  mkAffineFold Some.

(* GETTER *)

Record Getter S A := mkGetter
{ view : S -> A }.

Arguments mkGetter [S A].
Arguments view [S A].

Definition gtVerCompose {S A B}
    (gt1 : Getter S A) (gt2 : Getter A B) : Getter S B :=
  mkGetter (view gt2 ∘ view gt1).

Definition getHorCompose {S A B}
    (gt1 : Getter S A) (gt2 : Getter S B) : Getter S (A * B) :=
  mkGetter (fork (view gt1) (view gt2)).

Definition gtAsFold1 {S A} (gt : Getter S A) : Fold1 S A :=
  mkFold1 (fun _ _ f => f ∘ view gt).

Definition gtAsAffineFold {S A} (gt : Getter S A) : AffineFold S A :=
  mkAffineFold (Some ∘ view gt).

Definition idGt {S : Type} : Getter S S :=
  mkGetter id.

(* Notice that the following combinators build [AffineFold]s but they are placed
   here because of the dependency with [Getter] *)

(* XXX: the gt parameter is not standard in optics but it's practical to deal
   with product types. *)
Definition filtered' {S A} (gt : Getter S A) (p : A -> bool) : AffineFold S S :=
  mkAffineFold (fun s => if p (view gt s) then Some s else None).

Definition filtered {S} (p : S -> bool) : AffineFold S S :=
  filtered' idGt p.

(* TRAVERSAL1 *)

Definition result1 T A (n : nat) : Type :=
  t A (S n) * (t A (S n) -> T).

Record Traversal1 (S A : Type) := mkTraversal1
{ extract1 : S -> sigT (result1 S A) }.

Arguments mkTraversal1 [S A].
Arguments extract1 [S A].

Definition tr1VerCompose {S A B}
    (tr1 : Traversal1 S A) (tr2 : Traversal1 A B) : Traversal1 S B.
Proof. Admitted.

Definition tr1HorCompose {S A B}
    (tr1 : Traversal1 S A) (tr2 : Traversal1 S B) : Traversal1 S (A * B).
Proof. Admitted.

Definition tr1AsFold1 {S A} (tr1 : Traversal1 S A) : Fold1 S A :=
  mkFold1 (fun _ _ f s => match extract1 tr1 s with 
                          | existT _ _ (v, _) => fold1 f (vec1ToNel v)
                          end).

Definition tr1AsTraversal {S A} (tr1 : Traversal1 S A) : Traversal S A :=
  mkTraversal (fun s => match extract1 tr1 s with
                        | existT _ _ (v, f) => existT _ _ (v, f)
                        end).

(* AFFINE TRAVERSAL *)

Record AffineTraversal (S A : Type) := mkAffineTraversal
{ preview : S -> option A
; set : A -> S -> S
}.

Arguments mkAffineTraversal [S A].
Arguments preview [S A].
Arguments set [S A].

Definition atrVerCompose {S A B}
    (atr1 : AffineTraversal S A) (atr2 : AffineTraversal A B) : AffineTraversal S B :=
  mkAffineTraversal 
    (fun s => preview atr1 s >>= preview atr2) 
    (fun b s => option_fold (fun a => set atr1 (set atr2 b a) s) s (preview atr1 s)).

Definition atrHorCompose {S A B}
    (atr1 : AffineTraversal S A) (atr2 : AffineTraversal S B) : AffineTraversal S (A * B) :=
  mkAffineTraversal
    (fun s => tupled (preview atr1 s) (preview atr2 s))
    (fun ab => match ab with | (a, b) => set atr2 b ∘ set atr1 a end).

Definition atrAsAffineFold {S A} (atr : AffineTraversal S A) : AffineFold S A :=
  mkAffineFold (preview atr).

Definition atrAsTraversal {S A} (atr : AffineTraversal S A) : Traversal S A :=
  mkTraversal (fun s => option_fold 
    (fun a => existT _ _ (cons _ a _ (nil A), fun v => set atr (hd v) s)) 
    (existT  _ _ (nil A, fun _ => s)) 
    (preview atr s)).

Definition idAtr {S} : AffineTraversal S S :=
  mkAffineTraversal Some Basics.const.

(* LENS *)

Record Lens S A := mkLens
{ get : S -> A
; put : A -> S -> S
}.

Arguments mkLens [S A].
Arguments get [S A].
Arguments put [S A].

Definition lnVerCompose {S A B} (ln1 : Lens S A) (ln2 : Lens A B) : Lens S B :=
  mkLens (get ln2 ∘ get ln1) (fun b s => put ln1 (put ln2 b (get ln1 s)) s).

Definition lnHorCompose {S A B} 
    (ln1 : Lens S A) (ln2 : Lens S B) : Lens S (A * B) :=
  mkLens (fork (get ln1) (get ln2)) (fun ab => 
    match ab with | (a, b) => put ln2 b ∘ put ln1 a end).

Definition lnAsGetter {S A} (ln : Lens S A) : Getter S A :=
  mkGetter (get ln).

Definition lnAsAffineTraversal {S A} (ln : Lens S A) : AffineTraversal S A :=
  mkAffineTraversal (Some ∘ get ln) (put ln). 

Definition lnAsTraversal1 {S A} (ln : Lens S A) : Traversal1 S A :=
  mkTraversal1 (fun s => 
    existT _ _ (cons _ (get ln s) _ (nil _), fun v => put ln (hd v) s)).

Definition idLn {S : Type} : Lens S S :=
  mkLens id Basics.const.

Definition fstLn {A B : Type} : Lens (A * B) A :=
  mkLens fst (fun a ab => (a, snd ab)).

Definition sndLn {A B : Type} : Lens (A * B) B :=
  mkLens snd (fun b ab => (fst ab, b)).

(* PRISM *)

Record Prism S A := mkPrism
{ peek : S -> option A
; build : A -> S
}.

Arguments mkPrism [S A].
Arguments peek [S A].
Arguments build [S A].

Definition prVerCompose {S A B}
    (pr1 : Prism S A) (pr2 : Prism A B) : Prism S B :=
  mkPrism (fun s => peek pr1 s >>= peek pr2) (build pr1 ∘build pr2).

(* XXX: can't compose prisms horizontally! Notice how building, though possible, 
 * is lossy, because we would ignore one of the building methods.
 * 
 * Definition prHorCompose {S A B}
 *     (pr1 : Prism S A) (pr2 : Prism S B) : Prism S (A * B) :=
 *   mkPrism (fun s => (peek pr1 s, peek pr2 s))
 *           (fun ab => match ab with | (a, b) => ??? end)
 *)

Definition prAsAffineTraversal {S A} (pr : Prism S A) : AffineTraversal S A :=
  mkAffineTraversal (peek pr) (fun a _ => build pr a).

Definition idPr {S} : Prism S S :=
  mkPrism Some id.

(* ISO *)

Record Iso S A := mkIso
{ to : S -> A
; from : A -> S
}.

Arguments mkIso [S A].
Arguments to [S A].
Arguments from [S A].

Definition isoVerCompose {S A B}
    (iso1 : Iso S A) (iso2 : Iso A B) : Iso S B :=
  mkIso (to iso2 ∘ to iso1) (from iso1 ∘ from iso2).

(* XXX: can't compose isos horizontally!
 * 
 * Definition isoHorCompose {S A B}
 *     (iso1 : Iso S A) (iso2 : Iso S B) : Iso S (A * B) :=
 *   mkIso (fun s => (to iso1 s) (to iso2 s))
 *         (fun ab => match ab with | (a, b) => ? end).
 *)

Definition isoAsLens {S A} (iso : Iso S A) : Lens S A :=
  mkLens (to iso) (fun a _ => from iso a).

Definition isoAsPrism {S A} (iso : Iso S A) : Prism S A :=
  mkPrism (Some ∘ to iso) (from iso).

Definition idIso {S : Type} : Iso S S :=
  mkIso id id.

(* Optic class hierarchy *)

Class AsIso (op : Type -> Type -> Type) :=
{ asIso : forall {S A}, op S A -> Iso S A }.

Instance isoAsIso : AsIso Iso :=
{ asIso S A := id }.

Class AsLens (op : Type -> Type -> Type) :=
{ asLens : forall {S A}, op S A -> Lens S A }.

Instance lnToLens : AsLens Lens :=
{ asLens S A := id }.

Instance isoToLens `{AsIso op} : AsLens op :=
{ asLens S A := isoAsLens ∘ asIso }.

Class AsPrism (op : Type -> Type -> Type) :=
{ asPrism : forall {S A}, op S A -> Prism S A }.

Instance lnToPrism : AsPrism Prism :=
{ asPrism S A := id }.

Instance isoToPrism `{AsIso op} : AsPrism op :=
{ asPrism S A := isoAsPrism ∘ asIso }.

Class AsGetter (op : Type -> Type -> Type) :=
{ asGetter : forall {S A}, op S A -> Getter S A }.

Instance gtToGetter : AsGetter Getter :=
{ asGetter S A := id }.

Instance lnToGetter `{AsLens op} : AsGetter op :=
{ asGetter S A := lnAsGetter ∘ asLens }.

Class AsTraversal1 (op : Type -> Type -> Type) :=
{ asTraversal1 : forall {S A}, op S A -> Traversal1 S A }.

Instance tr1ToTraversal1 : AsTraversal1 Traversal1 :=
{ asTraversal1 S A := id }.

Instance lnToTraversal1 `{AsLens op} : AsTraversal1 op :=
{ asTraversal1 S A := lnAsTraversal1 ∘ asLens }.

Class AsAffineTraversal (op : Type -> Type -> Type) :=
{ asAffineTraversal : forall {S A}, op S A -> AffineTraversal S A }.

Instance atrToAffineTraversal : AsAffineTraversal AffineTraversal :=
{ asAffineTraversal S A := id }.

Instance lnToAffineTraversal `{AsLens op} : AsAffineTraversal op :=
{ asAffineTraversal S A := lnAsAffineTraversal ∘ asLens }.

Instance prToAffineTraversal `{AsPrism op} : AsAffineTraversal op :=
{ asAffineTraversal S A := prAsAffineTraversal ∘ asPrism }.

Class AsFold1 (op : Type -> Type -> Type) :=
{ asFold1 : forall {S A}, op S A -> Fold1 S A }.

Instance fl1ToFold1 : AsFold1 Fold1 :=
{ asFold1 S A := id }.

Instance gtToFold1 `{AsGetter op} : AsFold1 op :=
{ asFold1 S A := gtAsFold1 ∘ asGetter }.

Instance tr1ToFold1 `{AsTraversal1 op} : AsFold1 op :=
{ asFold1 S A := tr1AsFold1 ∘ asTraversal1 }.

Class AsAffineFold (op : Type -> Type -> Type) :=
{ asAffineFold : forall {S A}, op S A -> AffineFold S A }.

Instance fl1ToAffineFold : AsAffineFold AffineFold :=
{ asAffineFold S A := id }.

Instance gtToAffineFold `{AsGetter op} : AsAffineFold op :=
{ asAffineFold S A := gtAsAffineFold ∘ asGetter }.

Instance atrToAffineFold `{AsAffineTraversal op} : AsAffineFold op :=
{ asAffineFold S A := atrAsAffineFold ∘ asAffineTraversal }.

Class AsTraversal (op : Type -> Type -> Type) :=
{ asTraversal : forall {S A}, op S A -> Traversal S A }.

Instance fl1ToTraversal : AsTraversal Traversal :=
{ asTraversal S A := id }.

Instance atrToTraversal `{AsAffineTraversal op} : AsTraversal op :=
{ asTraversal S A := atrAsTraversal ∘ asAffineTraversal }.

Instance tr1ToTraversal `{AsTraversal1 op} : AsTraversal op :=
{ asTraversal S A := tr1AsTraversal ∘ asTraversal1 }.

Class AsSetter (op : Type -> Type -> Type) :=
{ asSetter : forall {S A}, op S A -> Setter S A }.

Instance stToSetter : AsSetter Setter :=
{ asSetter S A := id }.

Instance trToSetter `{AsTraversal op} : AsSetter op :=
{ asSetter S A := trAsSetter ∘ asTraversal }.

Class AsFold (op : Type -> Type -> Type) :=
{ asFold : forall {S A}, op S A -> Fold S A }.

Instance flToFold : AsFold Fold :=
{ asFold S A := id }.

Instance fl1ToFold `{AsFold1 op} : AsFold op :=
{ asFold S A := fl1AsFold ∘ asFold1 }.

Instance trToFold `{AsTraversal op} : AsFold op :=
{ asFold S A := trAsFold ∘ asTraversal }.

Instance aflToFold `{AsAffineFold op} : AsFold op :=
{ asFold S A := aflAsFold ∘ asAffineFold }.

(* VERTICAL COMPOSITION *)

Class VerCompose 
  (op1 : Type -> Type -> Type) 
  (op2 : Type -> Type -> Type)
  (res : Type -> Type -> Type) :=
{ verCompose : forall {S A B}, op1 S A -> op2 A B -> res S B }.

Notation "op1 › op2" := (verCompose op1 op2) (at level 50, left associativity).

Instance isoVerCom `{AsIso op1} `{AsIso op2} : VerCompose op1 op2 Iso :=
{ verCompose S A B op1 op2 :=
    isoVerCompose (asIso op1) (asIso op2)
}.

Instance lnVerCom `{AsLens op1} `{AsLens op2} : VerCompose op1 op2 Lens :=
{ verCompose S A B op1 op2 :=
    lnVerCompose (asLens op1) (asLens op2)
}.

Instance prVerCom `{AsPrism op1} `{AsPrism op2} : VerCompose op1 op2 Prism :=
{ verCompose S A B op1 op2 :=
    prVerCompose (asPrism op1) (asPrism op2)
}.

Instance gtVerCom `{AsGetter op1} `{AsGetter op2} : VerCompose op1 op2 Getter :=
{ verCompose S A B op1 op2 :=
    gtVerCompose (asGetter op1) (asGetter op2)
}.

Instance tr1VerCom `{AsTraversal1 op1} `{AsTraversal1 op2} : VerCompose op1 op2 Traversal1 :=
{ verCompose S A B op1 op2 :=
    tr1VerCompose (asTraversal1 op1) (asTraversal1 op2)
}.

Instance atrVerCom `{AsAffineTraversal op1} `{AsAffineTraversal op2} : VerCompose op1 op2 AffineTraversal :=
{ verCompose S A B op1 op2 :=
    atrVerCompose (asAffineTraversal op1) (asAffineTraversal op2)
}.

Instance fl1VerCom `{AsFold1 op1} `{AsFold1 op2} : VerCompose op1 op2 Fold1 :=
{ verCompose S A B op1 op2 :=
    fl1VerCompose (asFold1 op1) (asFold1 op2)
}.

Instance aflVerCom `{AsAffineFold op1} `{AsAffineFold op2} : VerCompose op1 op2 AffineFold :=
{ verCompose S A B op1 op2 :=
    aflVerCompose (asAffineFold op1) (asAffineFold op2)
}.

Instance trVerCom `{AsTraversal op1} `{AsTraversal op2} : VerCompose op1 op2 Traversal :=
{ verCompose S A B op1 op2 :=
    trVerCompose (asTraversal op1) (asTraversal op2)
}.

Instance flVerCom `{AsFold op1} `{AsFold op2} : VerCompose op1 op2 Fold :=
{ verCompose S A B op1 op2 :=
    flVerCompose (asFold op1) (asFold op2)
}.

Instance stVerCom `{AsSetter op1} `{AsSetter op2} : VerCompose op1 op2 Setter :=
{ verCompose S A B op1 op2 :=
    stVerCompose (asSetter op1) (asSetter op2)
}.

(* HORIZONTAL COMPOSITION *)

Class HorCompose
  (op1 : Type -> Type -> Type) 
  (op2 : Type -> Type -> Type)
  (res : Type -> Type -> Type) :=
{ horCompose : forall {S A B}, op1 S A -> op2 S B -> res S (prod A B) }.

(* digraph: 2h *)
Notation "op1 ⑂ op2" := (horCompose op1 op2) (at level 49, left associativity).

(* TODO: provide remaining instances *)

Instance flHorComp `{AsFold op1} `{AsFold op2} : HorCompose op1 op2 Fold :=
{ horCompose S A B fl1 fl2 := flHorCompose (asFold fl1) (asFold fl2)
}.

(* PRODUCT COMPOSITION *)

Class ProdCompose
  (op1 : Type -> Type -> Type) 
  (op2 : Type -> Type -> Type)
  (res : Type -> Type -> Type) :=
{ prodCompose : forall {S A B}, op1 S A -> op2 S B -> res S (prod A B) }.

Notation "op1 × op2" := (prodCompose op1 op2) (at level 48, left associativity).

(* TODO: provide remaining instances *)

Instance lnProdComp `{AsLens op1} `{AsLens op2} : ProdCompose op1 op2 Lens :=
{ prodCompose S A B ln1 ln2 := lnHorCompose (asLens ln1) (asLens ln2)
}.

Instance aflProdComp `{AsAffineFold op1} `{AsAffineFold op2} : ProdCompose op1 op2 AffineFold :=
{ prodCompose S A B afl1 afl2 := aflHorCompose (asAffineFold afl1) (asAffineFold afl2)
}.

Instance flProdComp `{AsFold op1} `{AsFold op2} : ProdCompose op1 op2 Fold :=
{ prodCompose S A B fl1 fl2 := flProCompose (asFold fl1) (asFold fl2)
}.

(* ACTIONS *)

Definition getAll {S A} `{AsFold op} (fl : op S A) : S -> list A :=
  foldMap (asFold fl) _ _ _ pure.

Definition getHead {S A} `{AsFold op} (fl : op S A) : S -> option A :=
  fun s => List.hd_error (getAll fl s).

Definition all {S A} `{AsFold op} (fl : op S A) (f : A -> bool) : S -> bool :=
  foldMap (asFold fl) _ _ _ f. 

Definition any {S A} `{AsFold op} (fl : op S A) (f : A -> bool) : S -> bool :=
  unwrapBool ∘ foldMap (asFold fl) _ _ _ (wrapBool ∘ f).

Definition contains {S A} `{AsFold op} `{Eq A} (fl : op S A) (a : A) : S -> bool :=
  any fl (eqb a).

(*******************)
(* COUPLES EXAMPLE *)
(*******************)

(* Data layer *)

Record Person := mkPerson
{ name : string
; age : nat
}.

Record Couple := mkCouple
{ her : Person
; him : Person
}.

Definition nameLn : Lens Person string :=
  mkLens name (fun s => mkPerson s ∘ age).

Definition ageLn : Lens Person nat :=
  mkLens age (fun n p => mkPerson (name p) n).

Definition herLn : Lens Couple Person :=
  mkLens her (fun p => mkCouple p ∘ him).

Definition himLn : Lens Couple Person :=
  mkLens him (fun p c => mkCouple (her c) p).

(* Logic *)

Definition getPeople : list Person -> list Person :=
  getAll each.

Definition getPeopleName : list Person -> list string :=
  getAll (each › nameLn).

Definition getPeopleNameAndAge : list Person -> list (string * nat) :=
  getAll (each › nameLn × ageLn).

Definition getPeopleGt30 : list Person -> list string :=
  getAll (each › nameLn × (ageLn › filtered (Nat.leb 30)) › fstLn).

Definition getPeopleGt30' : list Person -> list string :=
  getAll (each › nameLn × ageLn › filtered' (asGetter sndLn) (Nat.leb 30) › fstLn).

Definition subGt : Getter (nat * nat) nat := 
  mkGetter (fun ab => match ab with | (a, b) => a - b end).

Definition difference : list Couple -> list (string * nat) :=
  getAll (each › (herLn › nameLn) × 
    ((herLn › ageLn) × (himLn › ageLn) › subGt › filtered (Nat.ltb 0))).

Definition nat_in_range (x y n : nat) : bool :=
  Nat.leb x n && Nat.ltb n y.

Definition rangeFl (x y : nat) : Fold (list Person) string :=
  each › nameLn × (ageLn › filtered (nat_in_range x y)) › fstLn.

Definition getAgeFl (s : string) : Fold (list Person) nat :=
  each › (nameLn › filtered (eqb s)) × ageLn › sndLn.

Definition compose (s t : string) (xs : list Person) : list string :=
  option_fold 
    (fun xy => match xy with | (x, y) => getAll (rangeFl x y) xs end) 
    List.nil 
    (getHead (getAgeFl s ⑂ getAgeFl t) xs).

(* Test *)

Open Scope string_scope.

Definition alex := mkPerson "Alex" 60.
Definition bert := mkPerson "Bert" 55.
Definition cora := mkPerson "Cora" 33.
Definition drew := mkPerson "Drew" 31.
Definition edna := mkPerson "Edna" 21.
Definition fred := mkPerson "Fred" 60.

Definition people : list Person :=
  alex :: bert :: cora :: drew :: edna :: fred :: List.nil.

Definition couples : list Couple :=
  mkCouple alex bert ::
  mkCouple cora drew ::
  mkCouple edna fred :: List.nil.

Example test1 : getPeople people = people.
Proof. auto. Qed.

Example test2 : getPeopleName people = List.map name people.
Proof. auto. Qed.

Example test3 : getPeopleNameAndAge people = List.map (fun p => (name p, age p)) people.
Proof. auto. Qed.

Example test4 : 
  getPeopleGt30 people = "Alex" :: "Bert" :: "Cora" :: "Drew" :: "Fred" :: List.nil.
Proof. auto. Qed.

Example test5 : getPeopleGt30 people = getPeopleGt30' people.
Proof. auto. Qed.

Example test6 : difference couples = ("Alex", 5) :: ("Cora", 2) :: List.nil.
Proof. auto. Qed.

Example test7 : getAll (rangeFl 30 40) people = "Cora" :: "Drew" :: List.nil.
Proof. auto. Qed.

Example test8 : getHead (getAgeFl "Alex") people = Some 60.
Proof. auto. Qed.

Example test9 : compose "Edna" "Bert" people = 
  (* XXX: almost there! It seems order is reversed somewhere... Therefore rev *)
  List.rev ("Edna" :: "Drew" :: "Cora" :: List.nil).
Proof. auto. Qed.

(* Department example *)

Definition Task : Type := string.

Record Employee := mkEmployee
{ emp : string
; tasks : list Task
}.

Record Department := mkDepartment
{ dpt : string
; employees : list Employee
}.

Definition NestedOrg := list Department.

Definition empGt := mkGetter emp.
Definition tasksGt := mkGetter tasks.
Definition dptGt := mkGetter dpt.  
Definition employeesGt := mkGetter employees.

Definition expertise (tsk : Task) : NestedOrg -> list string :=
  getAll (each › filtered' employeesGt (
    all each (contains (tasksGt › each) tsk)) › dptGt).

Definition employeeDepartment : NestedOrg -> list (string * string) :=
  getAll (each › dptGt × (employeesGt › each › empGt)).

(* Notice how this version is a cartesian product among all departments and all
 * employees.
 *)
Definition employeeDepartment' : NestedOrg -> list (string * string) :=
  getAll ((each › dptGt) × (each › employeesGt › each › empGt)).

Definition alex' := mkEmployee "Alex" ("build" :: List.nil).
Definition bert' := mkEmployee "Bert" ("build" :: List.nil).
Definition cora' := mkEmployee "Cora" ("abstract" :: "build" :: "design" :: List.nil).
Definition drew' := mkEmployee "Drew" ("abstract" :: "design" :: List.nil).
Definition edna' := mkEmployee "Edna" ("abstract" :: "call" :: List.nil).
Definition fred' := mkEmployee "Fred" ("call" :: List.nil).

Definition product := mkDepartment "Product" (alex' :: bert' :: List.nil).
Definition research := mkDepartment "Research" (cora' :: drew' :: edna' :: List.nil).
Definition sales := mkDepartment "Sales" (fred' :: List.nil).
Definition quality := mkDepartment "Quality" List.nil.

Definition org : NestedOrg :=
  product :: research :: sales :: quality :: List.nil.

Example test10 : expertise "abstract" org = "Research" :: "Quality" :: List.nil.
Proof. auto. Qed.

Example test11 : employeeDepartment org =
  ("Product", "Alex") :: 
    ("Product", "Bert") :: 
    ("Research", "Cora") :: 
    ("Research", "Drew") :: 
    ("Research", "Edna") :: 
    ("Sales", "Fred") :: List.nil.
Proof. auto. Qed.

(******************************)
(* Finally, an optic language *)
(******************************)

Class OpticLang (expr : Type -> Type) :=

{ (* higher-order abstract syntax (hoas) *)
  lift : forall {A : Type}, A -> expr A
; lam : forall {A B : Type}, (expr A -> expr B) -> expr (A -> B)
; app : forall {A B : Type}, expr (A -> B) -> expr A -> expr B

  (* product-related primitives *)
; curry : forall {A B C : Type}, expr (A * B -> C) -> expr (A -> B -> C)
; uncurry : forall {A B C : Type}, expr (A -> B -> C) -> expr (A * B -> C)
; product : forall {A B : Type}, expr A -> expr B -> expr (prod A B)

  (* basic types *)
; ntr : nat -> expr nat
; str : string -> expr string

 (* lens-related primitives *)
; lens : forall {S A : Type}, Lens S A -> expr (Lens S A)
; lnAsGetter : forall {S A : Type}, expr (Lens S A) -> expr (Getter S A)
; lnAsTraversal : forall {S A : Type}, expr (Lens S A) -> expr (Traversal S A)
; lnComposeHoriz : forall {S A B : Type},
    expr (Lens S A) -> expr (Lens S B) -> expr (Lens S (A * B))
; lnComposeVerti : forall {S A B : Type},
    expr (Lens S A) -> expr (Lens A B) -> expr (Lens S B)

  (* traversal-related primitives *)
; traversal : forall {S A : Type}, Traversal S A -> expr (Traversal S A)
; trAsFold : forall {S A : Type}, expr (Traversal S A) -> expr (Fold S A)
; trComposeHoriz : forall {S A B : Type},
    expr (Traversal S A) -> expr (Traversal S B) -> expr (Traversal S (A * B))
; trComposeVerti : forall {S A B : Type},
    expr (Traversal S A) -> expr (Traversal A B) -> expr (Traversal S B)
; trComposeHorizL : forall {S A B : Type},
    expr (Traversal S A) -> expr (Traversal S B) -> expr (Traversal S (A * option B))
; trComposeHorizR : forall {S A B : Type},
    expr (Traversal S A) -> expr (Traversal S B) -> expr (Traversal S (option A * B))
; unsafeFiltered : forall {S A : Type},
    expr (Getter S A) -> expr (A -> Prop) -> expr (Traversal S S)

  (* getter-related primitives *)
; getter : forall {S A : Type}, expr (S -> A) -> expr (Getter S A)
; gtAsFold : forall {S A : Type}, expr (Getter S A) -> expr (Fold S A)
; gtComposeHoriz : forall {S A B : Type},
    expr (Getter S A) -> expr (Getter S B) -> expr (Getter S (A * B))
; gtComposeVerti : forall {S A B : Type},
    expr (Getter S A) -> expr (Getter A B) -> expr (Getter S B)

  (* propositional primitives and generic methods *)
; and : expr Prop -> expr Prop -> expr Prop
; or  : expr Prop -> expr Prop -> expr Prop
; leqt : expr nat -> expr nat -> expr Prop
; lt : expr nat -> expr nat -> expr Prop
; eq : expr string -> expr string -> expr Prop
; sub : expr (prod nat nat -> nat)
; upper : expr (string -> string)
; incr : expr (nat -> nat)
; append : forall {A : Type}, expr A -> expr (list A) -> expr (list A)
; identity {A : Type} : expr (A -> A)
; first {A B : Type} : expr (A * B -> A)
; second {A B : Type} : expr (A * B -> B)

  (* affine fold-related primitives*)
; affineFold : forall {S A : Type}, AffineFold S A -> expr (AffineFold S A)
; aflAsFold : forall {S A : Type}, expr (AffineFold S A) -> expr (Fold S A)
; afolding : forall {S A : Type}, expr (S -> option A) -> expr (AffineFold S A)
; filtered : forall {S A : Type}, expr (Getter S A) -> expr (A -> Prop) -> expr (AffineFold S S)

  (* fold-related primitives *)
; fold : forall {S A : Type}, Fold S A -> expr (Fold S A)
; flComposeHoriz : forall {S A B : Type},
    expr (Fold S A) -> expr (Fold S B) -> expr (Fold S (A * B))
; flComposeVerti : forall {S A B : Type},
    expr (Fold S A) -> expr (Fold A B) -> expr (Fold S B)
; flComposeHorizL : forall {S A B : Type},
    expr (Fold S A) -> expr (Fold S B) -> expr (Fold S (A * option B))
; flComposeHorizR : forall {S A B : Type},
    expr (Fold S A) -> expr (Fold S B) -> expr (Fold S (option A * B))

  (* action primitives *)
; foldM : forall {S A M : Type} `{Monoid M}, expr (Fold S A) -> expr (A -> M) -> expr (S -> M)
; getAll : forall {S A : Type}, expr (Fold S A) -> expr (S -> list A)
; getHead : forall {S A : Type}, expr (Fold S A) -> expr (S -> option A)
; all : forall {S A : Type}, expr (Fold S A) -> expr (A -> Prop) -> expr (S -> Prop)
; any : forall {S A : Type}, expr (Fold S A) -> expr (A -> Prop) -> expr (S -> Prop)
; contains : forall {S A : Type}, expr (Fold S A) -> expr A -> expr (S -> Prop)
; putAll : forall {S A : Type}, expr (Traversal S A) -> expr A -> expr (S -> S)
; modifyAll : forall {S A : Type}, expr (Traversal S A) -> expr (A -> A) -> expr (S -> S)

  (* derived methods *)
; firstLn {A B : Type} : expr (Lens (A * B) A) := lens fstLn
; secondLn {A B : Type} : expr (Lens (A * B) B) := lens sndLn
; firstGt {A B : Type} : expr (Getter (A * B) A) := getter first
; secondGt {A B : Type} : expr (Getter (A * B) B) := getter second
; lnAsFold {S A : Type} (ln : expr (Lens S A)) : expr (Fold S A) := trAsFold (lnAsTraversal ln)
; filtered' {S : Type} (p : expr (S -> Prop)) : expr (AffineFold S S) := filtered (getter identity) p
}.

Notation "ln1 +_ln ln2" := (lnComposeVerti ln1 ln2) (at level 50, left associativity).
Notation "ln1 *_ln ln2" := (lnComposeHoriz ln1 ln2) (at level 40, left associativity).

Notation "tr1 +_tr tr2" := (trComposeVerti tr1 tr2) (at level 50, left associativity).
Notation "tr1 *_tr tr2" := (trComposeHoriz tr1 tr2) (at level 40, left associativity).

Notation "gt1 +_gt gt2" := (gtComposeVerti gt1 gt2) (at level 50, left associativity).
Notation "gt1 *_gt gt2" := (gtComposeHoriz gt1 gt2) (at level 40, left associativity).

Notation "fl1 +_fl fl2" := (flComposeVerti fl1 fl2) (at level 50, left associativity).
Notation "fl1 *_fl fl2" := (flComposeHoriz fl1 fl2) (at level 40, left associativity).

Notation "n1 <= n2" := (leqt n1 n2) (at level 70, no associativity).
Notation "n1 < n2" := (lt n1 n2) (at level 70, no associativity).
Notation "n1 == n2" := (eq n1 n2) (at level 70, no associativity).
Notation "p /\ q" := (and p q) (at level 80, right associativity).

Notation "a |*| b" := (product a b) (at level 40, left associativity).

(*******************)
(* Couples example *)
(*******************)

Record Person := mkPerson
{ name: string
; age: nat
}.

Record Couple := mkCouple
{ her: Person
; him: Person
}.

Definition nameLn `{OpticLang expr} : expr (Lens Person string). 
Proof. Admitted.

Definition ageLn `{OpticLang expr} : expr (Lens Person nat). 
Proof. Admitted.

Definition herLn `{OpticLang expr} : expr (Lens Couple Person). 
Proof. Admitted.

Definition himLn `{OpticLang expr} : expr (Lens Couple Person).
Proof. Admitted.

Definition peopleTr `{OpticLang expr} : expr (Traversal (list Person) Person).
Proof. Admitted.

Definition couplesTr `{OpticLang expr} : expr (Traversal (list Couple) Couple).
Proof. Admitted.

Definition bothTr `{OpticLang expr} : expr (Traversal (list Couple) Person).
Proof. Admitted.

(* Query [getPeople], already normalized *)

Definition getPeople `{OpticLang expr} : expr (list Person -> list Person) :=
  getAll (trAsFold peopleTr).

(* Query [getName] *)

Definition personNameTr `{OpticLang expr} : expr (Traversal (list Person) string) :=
  peopleTr +_tr lnAsTraversal nameLn.

Definition getName `{OpticLang expr} : expr (list Person -> list string) :=
  getAll (trAsFold personNameTr).

(* Todos mis amigos se llaman Cayetano ~ https://www.youtube.com/watch?v=ZiUhV12G024  *)
Definition putName `{OpticLang expr} : expr (list Person -> list Person) :=
  putAll personNameTr (str "Cayetano").

Definition modifyName `{OpticLang expr} : expr (list Person -> list Person) :=
  modifyAll personNameTr upper.

(* Query [getAgeAndName] *)

Definition personAgeAndNameTr `{OpticLang expr} : expr (Traversal (list Person) (nat * string)) :=
  peopleTr +_tr lnAsTraversal (ageLn *_ln nameLn).

Definition getAgeAndName `{OpticLang expr} : expr (list Person -> list (nat * string)) :=
  getAll (trAsFold personAgeAndNameTr).

Definition putAgeAndName `{OpticLang expr} : expr (list Person -> list Person) :=
  putAll personAgeAndNameTr (ntr 33 |*| str "Cayetano").

(* Query [getHerAges] *)

Definition herAgesTr `{OpticLang expr} : expr (Traversal (list Couple) nat) :=
  couplesTr +_tr lnAsTraversal (herLn +_ln ageLn).

Definition getHerAges `{OpticLang expr} : expr (list Couple -> list nat) :=
  getAll (trAsFold herAgesTr).

Definition putHerAges `{OpticLang expr} : expr (list Couple -> list Couple) :=
  putAll herAgesTr (ntr 33).

Definition modifyHerAges `{OpticLang expr} : expr (list Couple -> list Couple) :=
  modifyAll herAgesTr incr.

(* Query [getPeopleOnTheirThirties] *)

Definition peopleOnTheirThirtiesTr `{OpticLang expr} : expr (Traversal (list Person) Person) :=
  peopleTr +_tr unsafeFiltered (lnAsGetter ageLn) (lam (fun a => ntr 30 <= a /\ a < ntr 40)).

Definition getPeopleOnTheirThirties `{OpticLang expr} : expr (list Person -> list Person) :=
  getAll (trAsFold peopleOnTheirThirtiesTr).

(* This is safe, since Cayetano is 33 years old, and therefore traversal laws hold. *)
Definition putPeopleOnTheirThirties `{OpticLang expr} : expr (list Person -> list Person) :=
  putAll peopleOnTheirThirtiesTr (lift (mkPerson "Cayetano" 33)).

(* Query [difference] *)

Definition difference `{OpticLang expr} :=
  getAll (trAsFold couplesTr +_fl
    lnAsFold (herLn +_ln nameLn) *_fl 
      gtAsFold (lnAsGetter ((herLn +_ln ageLn) *_ln (himLn +_ln ageLn)) +_gt getter sub) +_fl
    aflAsFold (filtered secondGt (lam (leqt (ntr 0))))).

(* Query [range] *)

Definition rangeFl `{OpticLang expr} (a b : expr nat) : expr (Fold (list Couple) string) :=
  trAsFold (bothTr +_tr lnAsTraversal (nameLn *_ln ageLn)) +_fl
    aflAsFold (filtered secondGt (lam (fun i => a <= i /\ i < b))) +_fl
    lnAsFold firstLn.

(* Query [getAge] *)

Definition getAgeFl `{OpticLang expr} (s : expr string) : expr (Fold (list Couple) nat) :=
  trAsFold (bothTr +_tr lnAsTraversal (nameLn *_ln ageLn)) +_fl
    aflAsFold (filtered firstGt (lam (fun n => n == s))) +_fl
    lnAsFold secondLn.

(* Query [compose] *)

Definition bind `{OpticLang expr} {S A M} `{Monoid M} 
    (fl : expr (Fold S A)) (f : expr A -> expr (S -> M)) : expr (S -> M) :=
  lam (fun s => app (foldM fl (lam (fun a => app (app (lam f) a) s))) s).

Notation "fl >>= f" := (bind fl f) (at level 40, left associativity).

Definition compose `{OpticLang expr} 
    (s t : expr string) : expr (list Couple -> list string) :=
  getAgeFl s >>= (fun a1 => getAgeFl t >>= (fun a2 => getAll (rangeFl a1 a2))).

Definition compose' `{OpticLang expr} 
    (s t : expr string) : expr (list Couple -> list string) :=
  getAgeFl s *_fl getAgeFl t >>= (getAll ∘ (app (uncurry (lam (lam ∘ rangeFl))))).

Notation "'do' a ← e ; c" := (e >>= (fun a => c)) (at level 60, right associativity).

Definition compose_do `{OpticLang expr} 
    (s t : expr string) : expr (list Couple -> list string) :=
  do a1 ← getAgeFl s;
  do a2 ← getAgeFl t;
  getAll (rangeFl a1 a2).

Definition compose'_do `{OpticLang expr} 
    (s t : expr string) : expr (list Couple -> list string) :=
  do ages ← getAgeFl s *_fl getAgeFl t;
  getAll (app (uncurry (lam (lam ∘ rangeFl))) ages).

(**********************)
(* Department example *)
(**********************)

Definition Task : Type := string.

Record Employee := mkEmployee
{ emp : string
; tasks : list Task
}.

Record Department := mkNestedOrg
{ dpt : string
; employees : list Employee
}.

Definition NestedOrg := list Department.

Definition eachTr {A : Type} `{OpticLang expr} : expr (Traversal (list A) A).
Proof. Admitted.

Definition eachFl {A : Type} `{OpticLang expr} : expr (Fold (list A) A).
Proof. Admitted.

Definition empLn `{OpticLang expr} : expr (Lens Employee string).
Proof. Admitted.

Definition tasksLn `{OpticLang expr} : expr (Lens Employee (list Task)).
Proof. Admitted.

Definition dptLn `{OpticLang expr} : expr (Lens Department string).
Proof. Admitted.

Definition employeesLn `{OpticLang expr} : expr (Lens Department (list Employee)).
Proof. Admitted.

(* Query [expertise] *)

Definition expertise `{OpticLang expr} (tsk : expr Task) : expr (NestedOrg -> list string) :=
  getAll (eachFl +_fl
    aflAsFold (filtered (lnAsGetter employeesLn)
      (all eachFl (contains (lnAsFold tasksLn +_fl eachFl) tsk))) +_fl
    lnAsFold dptLn).

(* Bonus: Query [insertCayetano] *)

(* We could use this trick to insert new values, while working with optics *)

(* Cayetano works in all departments *)
Definition insertCayetano `{OpticLang expr} : expr (NestedOrg -> NestedOrg) :=
  modifyAll (eachTr +_tr lnAsTraversal employeesLn)
            (lam (append (lift (mkEmployee "Cayetano" List.nil)))).

