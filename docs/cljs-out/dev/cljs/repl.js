// Compiled by ClojureScript 1.11.132 {:optimizations :none}
goog.provide('cljs.repl');
goog.require('cljs.core');
goog.require('cljs.spec.alpha');
goog.require('goog.string');
goog.require('goog.string.format');
cljs.repl.print_doc = (function cljs$repl$print_doc(p__16539){
var map__16540 = p__16539;
var map__16540__$1 = cljs.core.__destructure_map.call(null,map__16540);
var m = map__16540__$1;
var n = cljs.core.get.call(null,map__16540__$1,new cljs.core.Keyword(null,"ns","ns",441598760));
var nm = cljs.core.get.call(null,map__16540__$1,new cljs.core.Keyword(null,"name","name",1843675177));
cljs.core.println.call(null,"-------------------------");

cljs.core.println.call(null,(function (){var or__5002__auto__ = new cljs.core.Keyword(null,"spec","spec",347520401).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return [(function (){var temp__5823__auto__ = new cljs.core.Keyword(null,"ns","ns",441598760).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(temp__5823__auto__)){
var ns = temp__5823__auto__;
return [cljs.core.str.cljs$core$IFn$_invoke$arity$1(ns),"/"].join('');
} else {
return null;
}
})(),cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join('');
}
})());

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Protocol");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m))){
var seq__16541_16569 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m));
var chunk__16542_16570 = null;
var count__16543_16571 = (0);
var i__16544_16572 = (0);
while(true){
if((i__16544_16572 < count__16543_16571)){
var f_16573 = cljs.core._nth.call(null,chunk__16542_16570,i__16544_16572);
cljs.core.println.call(null,"  ",f_16573);


var G__16574 = seq__16541_16569;
var G__16575 = chunk__16542_16570;
var G__16576 = count__16543_16571;
var G__16577 = (i__16544_16572 + (1));
seq__16541_16569 = G__16574;
chunk__16542_16570 = G__16575;
count__16543_16571 = G__16576;
i__16544_16572 = G__16577;
continue;
} else {
var temp__5823__auto___16578 = cljs.core.seq.call(null,seq__16541_16569);
if(temp__5823__auto___16578){
var seq__16541_16579__$1 = temp__5823__auto___16578;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__16541_16579__$1)){
var c__5525__auto___16580 = cljs.core.chunk_first.call(null,seq__16541_16579__$1);
var G__16581 = cljs.core.chunk_rest.call(null,seq__16541_16579__$1);
var G__16582 = c__5525__auto___16580;
var G__16583 = cljs.core.count.call(null,c__5525__auto___16580);
var G__16584 = (0);
seq__16541_16569 = G__16581;
chunk__16542_16570 = G__16582;
count__16543_16571 = G__16583;
i__16544_16572 = G__16584;
continue;
} else {
var f_16585 = cljs.core.first.call(null,seq__16541_16579__$1);
cljs.core.println.call(null,"  ",f_16585);


var G__16586 = cljs.core.next.call(null,seq__16541_16579__$1);
var G__16587 = null;
var G__16588 = (0);
var G__16589 = (0);
seq__16541_16569 = G__16586;
chunk__16542_16570 = G__16587;
count__16543_16571 = G__16588;
i__16544_16572 = G__16589;
continue;
}
} else {
}
}
break;
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m))){
var arglists_16590 = new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_((function (){var or__5002__auto__ = new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m);
}
})())){
cljs.core.prn.call(null,arglists_16590);
} else {
cljs.core.prn.call(null,((cljs.core._EQ_.call(null,new cljs.core.Symbol(null,"quote","quote",1377916282,null),cljs.core.first.call(null,arglists_16590)))?cljs.core.second.call(null,arglists_16590):arglists_16590));
}
} else {
}
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"special-form","special-form",-1326536374).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Special Form");

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.contains_QMARK_.call(null,m,new cljs.core.Keyword(null,"url","url",276297046))){
if(cljs.core.truth_(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))){
return cljs.core.println.call(null,["\n  Please see http://clojure.org/",cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))].join(''));
} else {
return null;
}
} else {
return cljs.core.println.call(null,["\n  Please see http://clojure.org/special_forms#",cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Macro");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"spec","spec",347520401).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Spec");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"REPL Special Function");
} else {
}

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
var seq__16545_16591 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"methods","methods",453930866).cljs$core$IFn$_invoke$arity$1(m));
var chunk__16546_16592 = null;
var count__16547_16593 = (0);
var i__16548_16594 = (0);
while(true){
if((i__16548_16594 < count__16547_16593)){
var vec__16557_16595 = cljs.core._nth.call(null,chunk__16546_16592,i__16548_16594);
var name_16596 = cljs.core.nth.call(null,vec__16557_16595,(0),null);
var map__16560_16597 = cljs.core.nth.call(null,vec__16557_16595,(1),null);
var map__16560_16598__$1 = cljs.core.__destructure_map.call(null,map__16560_16597);
var doc_16599 = cljs.core.get.call(null,map__16560_16598__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_16600 = cljs.core.get.call(null,map__16560_16598__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_16596);

cljs.core.println.call(null," ",arglists_16600);

if(cljs.core.truth_(doc_16599)){
cljs.core.println.call(null," ",doc_16599);
} else {
}


var G__16601 = seq__16545_16591;
var G__16602 = chunk__16546_16592;
var G__16603 = count__16547_16593;
var G__16604 = (i__16548_16594 + (1));
seq__16545_16591 = G__16601;
chunk__16546_16592 = G__16602;
count__16547_16593 = G__16603;
i__16548_16594 = G__16604;
continue;
} else {
var temp__5823__auto___16605 = cljs.core.seq.call(null,seq__16545_16591);
if(temp__5823__auto___16605){
var seq__16545_16606__$1 = temp__5823__auto___16605;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__16545_16606__$1)){
var c__5525__auto___16607 = cljs.core.chunk_first.call(null,seq__16545_16606__$1);
var G__16608 = cljs.core.chunk_rest.call(null,seq__16545_16606__$1);
var G__16609 = c__5525__auto___16607;
var G__16610 = cljs.core.count.call(null,c__5525__auto___16607);
var G__16611 = (0);
seq__16545_16591 = G__16608;
chunk__16546_16592 = G__16609;
count__16547_16593 = G__16610;
i__16548_16594 = G__16611;
continue;
} else {
var vec__16561_16612 = cljs.core.first.call(null,seq__16545_16606__$1);
var name_16613 = cljs.core.nth.call(null,vec__16561_16612,(0),null);
var map__16564_16614 = cljs.core.nth.call(null,vec__16561_16612,(1),null);
var map__16564_16615__$1 = cljs.core.__destructure_map.call(null,map__16564_16614);
var doc_16616 = cljs.core.get.call(null,map__16564_16615__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_16617 = cljs.core.get.call(null,map__16564_16615__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_16613);

cljs.core.println.call(null," ",arglists_16617);

if(cljs.core.truth_(doc_16616)){
cljs.core.println.call(null," ",doc_16616);
} else {
}


var G__16618 = cljs.core.next.call(null,seq__16545_16606__$1);
var G__16619 = null;
var G__16620 = (0);
var G__16621 = (0);
seq__16545_16591 = G__16618;
chunk__16546_16592 = G__16619;
count__16547_16593 = G__16620;
i__16548_16594 = G__16621;
continue;
}
} else {
}
}
break;
}
} else {
}

if(cljs.core.truth_(n)){
var temp__5823__auto__ = cljs.spec.alpha.get_spec.call(null,cljs.core.symbol.call(null,cljs.core.str.cljs$core$IFn$_invoke$arity$1(cljs.core.ns_name.call(null,n)),cljs.core.name.call(null,nm)));
if(cljs.core.truth_(temp__5823__auto__)){
var fnspec = temp__5823__auto__;
cljs.core.print.call(null,"Spec");

var seq__16565 = cljs.core.seq.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"args","args",1315556576),new cljs.core.Keyword(null,"ret","ret",-468222814),new cljs.core.Keyword(null,"fn","fn",-1175266204)], null));
var chunk__16566 = null;
var count__16567 = (0);
var i__16568 = (0);
while(true){
if((i__16568 < count__16567)){
var role = cljs.core._nth.call(null,chunk__16566,i__16568);
var temp__5823__auto___16622__$1 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__5823__auto___16622__$1)){
var spec_16623 = temp__5823__auto___16622__$1;
cljs.core.print.call(null,["\n ",cljs.core.name.call(null,role),":"].join(''),cljs.spec.alpha.describe.call(null,spec_16623));
} else {
}


var G__16624 = seq__16565;
var G__16625 = chunk__16566;
var G__16626 = count__16567;
var G__16627 = (i__16568 + (1));
seq__16565 = G__16624;
chunk__16566 = G__16625;
count__16567 = G__16626;
i__16568 = G__16627;
continue;
} else {
var temp__5823__auto____$1 = cljs.core.seq.call(null,seq__16565);
if(temp__5823__auto____$1){
var seq__16565__$1 = temp__5823__auto____$1;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__16565__$1)){
var c__5525__auto__ = cljs.core.chunk_first.call(null,seq__16565__$1);
var G__16628 = cljs.core.chunk_rest.call(null,seq__16565__$1);
var G__16629 = c__5525__auto__;
var G__16630 = cljs.core.count.call(null,c__5525__auto__);
var G__16631 = (0);
seq__16565 = G__16628;
chunk__16566 = G__16629;
count__16567 = G__16630;
i__16568 = G__16631;
continue;
} else {
var role = cljs.core.first.call(null,seq__16565__$1);
var temp__5823__auto___16632__$2 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__5823__auto___16632__$2)){
var spec_16633 = temp__5823__auto___16632__$2;
cljs.core.print.call(null,["\n ",cljs.core.name.call(null,role),":"].join(''),cljs.spec.alpha.describe.call(null,spec_16633));
} else {
}


var G__16634 = cljs.core.next.call(null,seq__16565__$1);
var G__16635 = null;
var G__16636 = (0);
var G__16637 = (0);
seq__16565 = G__16634;
chunk__16566 = G__16635;
count__16567 = G__16636;
i__16568 = G__16637;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return null;
}
} else {
return null;
}
}
});
/**
 * Constructs a data representation for a Error with keys:
 *  :cause - root cause message
 *  :phase - error phase
 *  :via - cause chain, with cause keys:
 *           :type - exception class symbol
 *           :message - exception message
 *           :data - ex-data
 *           :at - top stack element
 *  :trace - root cause stack elements
 */
cljs.repl.Error__GT_map = (function cljs$repl$Error__GT_map(o){
return cljs.core.Throwable__GT_map.call(null,o);
});
/**
 * Returns an analysis of the phase, error, cause, and location of an error that occurred
 *   based on Throwable data, as returned by Throwable->map. All attributes other than phase
 *   are optional:
 *  :clojure.error/phase - keyword phase indicator, one of:
 *    :read-source :compile-syntax-check :compilation :macro-syntax-check :macroexpansion
 *    :execution :read-eval-result :print-eval-result
 *  :clojure.error/source - file name (no path)
 *  :clojure.error/line - integer line number
 *  :clojure.error/column - integer column number
 *  :clojure.error/symbol - symbol being expanded/compiled/invoked
 *  :clojure.error/class - cause exception class symbol
 *  :clojure.error/cause - cause exception message
 *  :clojure.error/spec - explain-data for spec error
 */
cljs.repl.ex_triage = (function cljs$repl$ex_triage(datafied_throwable){
var map__16640 = datafied_throwable;
var map__16640__$1 = cljs.core.__destructure_map.call(null,map__16640);
var via = cljs.core.get.call(null,map__16640__$1,new cljs.core.Keyword(null,"via","via",-1904457336));
var trace = cljs.core.get.call(null,map__16640__$1,new cljs.core.Keyword(null,"trace","trace",-1082747415));
var phase = cljs.core.get.call(null,map__16640__$1,new cljs.core.Keyword(null,"phase","phase",575722892),new cljs.core.Keyword(null,"execution","execution",253283524));
var map__16641 = cljs.core.last.call(null,via);
var map__16641__$1 = cljs.core.__destructure_map.call(null,map__16641);
var type = cljs.core.get.call(null,map__16641__$1,new cljs.core.Keyword(null,"type","type",1174270348));
var message = cljs.core.get.call(null,map__16641__$1,new cljs.core.Keyword(null,"message","message",-406056002));
var data = cljs.core.get.call(null,map__16641__$1,new cljs.core.Keyword(null,"data","data",-232669377));
var map__16642 = data;
var map__16642__$1 = cljs.core.__destructure_map.call(null,map__16642);
var problems = cljs.core.get.call(null,map__16642__$1,new cljs.core.Keyword("cljs.spec.alpha","problems","cljs.spec.alpha/problems",447400814));
var fn = cljs.core.get.call(null,map__16642__$1,new cljs.core.Keyword("cljs.spec.alpha","fn","cljs.spec.alpha/fn",408600443));
var caller = cljs.core.get.call(null,map__16642__$1,new cljs.core.Keyword("cljs.spec.test.alpha","caller","cljs.spec.test.alpha/caller",-398302390));
var map__16643 = new cljs.core.Keyword(null,"data","data",-232669377).cljs$core$IFn$_invoke$arity$1(cljs.core.first.call(null,via));
var map__16643__$1 = cljs.core.__destructure_map.call(null,map__16643);
var top_data = map__16643__$1;
var source = cljs.core.get.call(null,map__16643__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397));
return cljs.core.assoc.call(null,(function (){var G__16644 = phase;
var G__16644__$1 = (((G__16644 instanceof cljs.core.Keyword))?G__16644.fqn:null);
switch (G__16644__$1) {
case "read-source":
var map__16645 = data;
var map__16645__$1 = cljs.core.__destructure_map.call(null,map__16645);
var line = cljs.core.get.call(null,map__16645__$1,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471));
var column = cljs.core.get.call(null,map__16645__$1,new cljs.core.Keyword("clojure.error","column","clojure.error/column",304721553));
var G__16646 = cljs.core.merge.call(null,new cljs.core.Keyword(null,"data","data",-232669377).cljs$core$IFn$_invoke$arity$1(cljs.core.second.call(null,via)),top_data);
var G__16646__$1 = (cljs.core.truth_(source)?cljs.core.assoc.call(null,G__16646,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),source):G__16646);
var G__16646__$2 = (cljs.core.truth_(new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["NO_SOURCE_PATH",null,"NO_SOURCE_FILE",null], null), null).call(null,source))?cljs.core.dissoc.call(null,G__16646__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397)):G__16646__$1);
if(cljs.core.truth_(message)){
return cljs.core.assoc.call(null,G__16646__$2,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message);
} else {
return G__16646__$2;
}

break;
case "compile-syntax-check":
case "compilation":
case "macro-syntax-check":
case "macroexpansion":
var G__16647 = top_data;
var G__16647__$1 = (cljs.core.truth_(source)?cljs.core.assoc.call(null,G__16647,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),source):G__16647);
var G__16647__$2 = (cljs.core.truth_(new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["NO_SOURCE_PATH",null,"NO_SOURCE_FILE",null], null), null).call(null,source))?cljs.core.dissoc.call(null,G__16647__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397)):G__16647__$1);
var G__16647__$3 = (cljs.core.truth_(type)?cljs.core.assoc.call(null,G__16647__$2,new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890),type):G__16647__$2);
var G__16647__$4 = (cljs.core.truth_(message)?cljs.core.assoc.call(null,G__16647__$3,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message):G__16647__$3);
if(cljs.core.truth_(problems)){
return cljs.core.assoc.call(null,G__16647__$4,new cljs.core.Keyword("clojure.error","spec","clojure.error/spec",2055032595),data);
} else {
return G__16647__$4;
}

break;
case "read-eval-result":
case "print-eval-result":
var vec__16648 = cljs.core.first.call(null,trace);
var source__$1 = cljs.core.nth.call(null,vec__16648,(0),null);
var method = cljs.core.nth.call(null,vec__16648,(1),null);
var file = cljs.core.nth.call(null,vec__16648,(2),null);
var line = cljs.core.nth.call(null,vec__16648,(3),null);
var G__16651 = top_data;
var G__16651__$1 = (cljs.core.truth_(line)?cljs.core.assoc.call(null,G__16651,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471),line):G__16651);
var G__16651__$2 = (cljs.core.truth_(file)?cljs.core.assoc.call(null,G__16651__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),file):G__16651__$1);
var G__16651__$3 = (cljs.core.truth_((function (){var and__5000__auto__ = source__$1;
if(cljs.core.truth_(and__5000__auto__)){
return method;
} else {
return and__5000__auto__;
}
})())?cljs.core.assoc.call(null,G__16651__$2,new cljs.core.Keyword("clojure.error","symbol","clojure.error/symbol",1544821994),(new cljs.core.PersistentVector(null,2,(5),cljs.core.PersistentVector.EMPTY_NODE,[source__$1,method],null))):G__16651__$2);
var G__16651__$4 = (cljs.core.truth_(type)?cljs.core.assoc.call(null,G__16651__$3,new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890),type):G__16651__$3);
if(cljs.core.truth_(message)){
return cljs.core.assoc.call(null,G__16651__$4,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message);
} else {
return G__16651__$4;
}

break;
case "execution":
var vec__16652 = cljs.core.first.call(null,trace);
var source__$1 = cljs.core.nth.call(null,vec__16652,(0),null);
var method = cljs.core.nth.call(null,vec__16652,(1),null);
var file = cljs.core.nth.call(null,vec__16652,(2),null);
var line = cljs.core.nth.call(null,vec__16652,(3),null);
var file__$1 = cljs.core.first.call(null,cljs.core.remove.call(null,(function (p1__16639_SHARP_){
var or__5002__auto__ = (p1__16639_SHARP_ == null);
if(or__5002__auto__){
return or__5002__auto__;
} else {
return new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["NO_SOURCE_PATH",null,"NO_SOURCE_FILE",null], null), null).call(null,p1__16639_SHARP_);
}
}),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"file","file",-1269645878).cljs$core$IFn$_invoke$arity$1(caller),file], null)));
var err_line = (function (){var or__5002__auto__ = new cljs.core.Keyword(null,"line","line",212345235).cljs$core$IFn$_invoke$arity$1(caller);
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return line;
}
})();
var G__16655 = new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890),type], null);
var G__16655__$1 = (cljs.core.truth_(err_line)?cljs.core.assoc.call(null,G__16655,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471),err_line):G__16655);
var G__16655__$2 = (cljs.core.truth_(message)?cljs.core.assoc.call(null,G__16655__$1,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message):G__16655__$1);
var G__16655__$3 = (cljs.core.truth_((function (){var or__5002__auto__ = fn;
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
var and__5000__auto__ = source__$1;
if(cljs.core.truth_(and__5000__auto__)){
return method;
} else {
return and__5000__auto__;
}
}
})())?cljs.core.assoc.call(null,G__16655__$2,new cljs.core.Keyword("clojure.error","symbol","clojure.error/symbol",1544821994),(function (){var or__5002__auto__ = fn;
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return (new cljs.core.PersistentVector(null,2,(5),cljs.core.PersistentVector.EMPTY_NODE,[source__$1,method],null));
}
})()):G__16655__$2);
var G__16655__$4 = (cljs.core.truth_(file__$1)?cljs.core.assoc.call(null,G__16655__$3,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),file__$1):G__16655__$3);
if(cljs.core.truth_(problems)){
return cljs.core.assoc.call(null,G__16655__$4,new cljs.core.Keyword("clojure.error","spec","clojure.error/spec",2055032595),data);
} else {
return G__16655__$4;
}

break;
default:
throw (new Error(["No matching clause: ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(G__16644__$1)].join('')));

}
})(),new cljs.core.Keyword("clojure.error","phase","clojure.error/phase",275140358),phase);
});
/**
 * Returns a string from exception data, as produced by ex-triage.
 *   The first line summarizes the exception phase and location.
 *   The subsequent lines describe the cause.
 */
cljs.repl.ex_str = (function cljs$repl$ex_str(p__16659){
var map__16660 = p__16659;
var map__16660__$1 = cljs.core.__destructure_map.call(null,map__16660);
var triage_data = map__16660__$1;
var phase = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","phase","clojure.error/phase",275140358));
var source = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397));
var line = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471));
var column = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","column","clojure.error/column",304721553));
var symbol = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","symbol","clojure.error/symbol",1544821994));
var class$ = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890));
var cause = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742));
var spec = cljs.core.get.call(null,map__16660__$1,new cljs.core.Keyword("clojure.error","spec","clojure.error/spec",2055032595));
var loc = [cljs.core.str.cljs$core$IFn$_invoke$arity$1((function (){var or__5002__auto__ = source;
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return "<cljs repl>";
}
})()),":",cljs.core.str.cljs$core$IFn$_invoke$arity$1((function (){var or__5002__auto__ = line;
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return (1);
}
})()),(cljs.core.truth_(column)?[":",cljs.core.str.cljs$core$IFn$_invoke$arity$1(column)].join(''):"")].join('');
var class_name = cljs.core.name.call(null,(function (){var or__5002__auto__ = class$;
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return "";
}
})());
var simple_class = class_name;
var cause_type = ((cljs.core.contains_QMARK_.call(null,new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["RuntimeException",null,"Exception",null], null), null),simple_class))?"":[" (",simple_class,")"].join(''));
var format = goog.string.format;
var G__16661 = phase;
var G__16661__$1 = (((G__16661 instanceof cljs.core.Keyword))?G__16661.fqn:null);
switch (G__16661__$1) {
case "read-source":
return format.call(null,"Syntax error reading source at (%s).\n%s\n",loc,cause);

break;
case "macro-syntax-check":
return format.call(null,"Syntax error macroexpanding %sat (%s).\n%s",(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,(cljs.core.truth_(spec)?(function (){var sb__5647__auto__ = (new goog.string.StringBuffer());
var _STAR_print_newline_STAR__orig_val__16662_16671 = cljs.core._STAR_print_newline_STAR_;
var _STAR_print_fn_STAR__orig_val__16663_16672 = cljs.core._STAR_print_fn_STAR_;
var _STAR_print_newline_STAR__temp_val__16664_16673 = true;
var _STAR_print_fn_STAR__temp_val__16665_16674 = (function (x__5648__auto__){
return sb__5647__auto__.append(x__5648__auto__);
});
(cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__temp_val__16664_16673);

(cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__temp_val__16665_16674);

try{cljs.spec.alpha.explain_out.call(null,cljs.core.update.call(null,spec,new cljs.core.Keyword("cljs.spec.alpha","problems","cljs.spec.alpha/problems",447400814),(function (probs){
return cljs.core.map.call(null,(function (p1__16657_SHARP_){
return cljs.core.dissoc.call(null,p1__16657_SHARP_,new cljs.core.Keyword(null,"in","in",-1531184865));
}),probs);
}))
);
}finally {(cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__orig_val__16663_16672);

(cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__orig_val__16662_16671);
}
return cljs.core.str.cljs$core$IFn$_invoke$arity$1(sb__5647__auto__);
})():format.call(null,"%s\n",cause)));

break;
case "macroexpansion":
return format.call(null,"Unexpected error%s macroexpanding %sat (%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);

break;
case "compile-syntax-check":
return format.call(null,"Syntax error%s compiling %sat (%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);

break;
case "compilation":
return format.call(null,"Unexpected error%s compiling %sat (%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);

break;
case "read-eval-result":
return format.call(null,"Error reading eval result%s at %s (%s).\n%s\n",cause_type,symbol,loc,cause);

break;
case "print-eval-result":
return format.call(null,"Error printing return value%s at %s (%s).\n%s\n",cause_type,symbol,loc,cause);

break;
case "execution":
if(cljs.core.truth_(spec)){
return format.call(null,"Execution error - invalid arguments to %s at (%s).\n%s",symbol,loc,(function (){var sb__5647__auto__ = (new goog.string.StringBuffer());
var _STAR_print_newline_STAR__orig_val__16666_16675 = cljs.core._STAR_print_newline_STAR_;
var _STAR_print_fn_STAR__orig_val__16667_16676 = cljs.core._STAR_print_fn_STAR_;
var _STAR_print_newline_STAR__temp_val__16668_16677 = true;
var _STAR_print_fn_STAR__temp_val__16669_16678 = (function (x__5648__auto__){
return sb__5647__auto__.append(x__5648__auto__);
});
(cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__temp_val__16668_16677);

(cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__temp_val__16669_16678);

try{cljs.spec.alpha.explain_out.call(null,cljs.core.update.call(null,spec,new cljs.core.Keyword("cljs.spec.alpha","problems","cljs.spec.alpha/problems",447400814),(function (probs){
return cljs.core.map.call(null,(function (p1__16658_SHARP_){
return cljs.core.dissoc.call(null,p1__16658_SHARP_,new cljs.core.Keyword(null,"in","in",-1531184865));
}),probs);
}))
);
}finally {(cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__orig_val__16667_16676);

(cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__orig_val__16666_16675);
}
return cljs.core.str.cljs$core$IFn$_invoke$arity$1(sb__5647__auto__);
})());
} else {
return format.call(null,"Execution error%s at %s(%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);
}

break;
default:
throw (new Error(["No matching clause: ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(G__16661__$1)].join('')));

}
});
cljs.repl.error__GT_str = (function cljs$repl$error__GT_str(error){
return cljs.repl.ex_str.call(null,cljs.repl.ex_triage.call(null,cljs.repl.Error__GT_map.call(null,error)));
});

//# sourceMappingURL=repl.js.map
