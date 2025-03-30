// Compiled by ClojureScript 1.11.132 {:optimizations :none}
goog.provide('learn_cljs.contacts');
goog.require('cljs.core');
goog.require('hiccups.runtime');
goog.require('goog.dom');
goog.require('goog.events');
goog.require('clojure.string');
learn_cljs.contacts.initial_state = new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"contacts","contacts",333503174),cljs.core.PersistentVector.EMPTY,new cljs.core.Keyword(null,"selected","selected",574897764),null,new cljs.core.Keyword(null,"editing?","editing?",1646440800),false], null);
learn_cljs.contacts.make_address = (function learn_cljs$contacts$make_address(address){
return cljs.core.select_keys.call(null,address,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"street","street",1870012303),new cljs.core.Keyword(null,"city","city",-393302614),new cljs.core.Keyword(null,"state","state",-1988618099),new cljs.core.Keyword(null,"postal","postal",-1824232834),new cljs.core.Keyword(null,"country","country",312965309)], null));
});
learn_cljs.contacts.maybe_set_address = (function learn_cljs$contacts$maybe_set_address(contact){
if(cljs.core.truth_(new cljs.core.Keyword(null,"address","address",559499426).cljs$core$IFn$_invoke$arity$1(contact))){
return cljs.core.update.call(null,contact,new cljs.core.Keyword(null,"address","address",559499426),learn_cljs.contacts.make_address);
} else {
return contact;
}
});
learn_cljs.contacts.make_contact = (function learn_cljs$contacts$make_contact(contact){
return learn_cljs.contacts.maybe_set_address.call(null,cljs.core.select_keys.call(null,contact,new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"first-name","first-name",-1559982131),new cljs.core.Keyword(null,"last-name","last-name",-1695738974),new cljs.core.Keyword(null,"email","email",1415816706),new cljs.core.Keyword(null,"address","address",559499426)], null)));
});
learn_cljs.contacts.add_contact = (function learn_cljs$contacts$add_contact(contact_list,input){
return cljs.core.conj.call(null,contact_list,learn_cljs.contacts.make_contact.call(null,input));
});
learn_cljs.contacts.replace_contact = (function learn_cljs$contacts$replace_contact(contact_list,idx,input){
return cljs.core.assoc.call(null,contact_list,idx,learn_cljs.contacts.make_contact.call(null,input));
});
learn_cljs.contacts.remove_contact = (function learn_cljs$contacts$remove_contact(contact_list,idx){
return cljs.core.vec.call(null,cljs.core.concat.call(null,cljs.core.subvec.call(null,contact_list,(0),idx),cljs.core.subvec.call(null,contact_list,(idx + (1)))));
});
learn_cljs.contacts.app_container = goog.dom.getElement("app");
learn_cljs.contacts.set_app_html_BANG_ = (function learn_cljs$contacts$set_app_html_BANG_(html_str){
return (learn_cljs.contacts.app_container.innerHTML = html_str);
});
learn_cljs.contacts.on_add_contact = (function learn_cljs$contacts$on_add_contact(state){
return learn_cljs.contacts.refresh_BANG_.call(null,cljs.core.dissoc.call(null,cljs.core.assoc.call(null,state,new cljs.core.Keyword(null,"editing?","editing?",1646440800),true),new cljs.core.Keyword(null,"selected","selected",574897764)));
});
learn_cljs.contacts.get_field_value = (function learn_cljs$contacts$get_field_value(id){
var value = goog.dom.getElement(id).value;
if(cljs.core.seq.call(null,value)){
return value;
} else {
return null;
}
});
learn_cljs.contacts.get_contact_form_data = (function learn_cljs$contacts$get_contact_form_data(){
return new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"first-name","first-name",-1559982131),learn_cljs.contacts.get_field_value.call(null,"input-first-name"),new cljs.core.Keyword(null,"last-name","last-name",-1695738974),learn_cljs.contacts.get_field_value.call(null,"input-last-name"),new cljs.core.Keyword(null,"email","email",1415816706),learn_cljs.contacts.get_field_value.call(null,"input-email"),new cljs.core.Keyword(null,"address","address",559499426),new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"street","street",1870012303),learn_cljs.contacts.get_field_value.call(null,"input-street"),new cljs.core.Keyword(null,"city","city",-393302614),learn_cljs.contacts.get_field_value.call(null,"input-city"),new cljs.core.Keyword(null,"state","state",-1988618099),learn_cljs.contacts.get_field_value.call(null,"input-state"),new cljs.core.Keyword(null,"postal","postal",-1824232834),learn_cljs.contacts.get_field_value.call(null,"input-postal"),new cljs.core.Keyword(null,"country","country",312965309),learn_cljs.contacts.get_field_value.call(null,"input-country")], null)], null);
});
learn_cljs.contacts.on_save_contact = (function learn_cljs$contacts$on_save_contact(state){
return learn_cljs.contacts.refresh_BANG_.call(null,(function (){var contact = learn_cljs.contacts.get_contact_form_data.call(null);
var idx = new cljs.core.Keyword(null,"selected","selected",574897764).cljs$core$IFn$_invoke$arity$1(state);
var state__$1 = cljs.core.dissoc.call(null,state,new cljs.core.Keyword(null,"selected","selected",574897764),new cljs.core.Keyword(null,"editing?","editing?",1646440800));
if(cljs.core.truth_(idx)){
return cljs.core.update.call(null,state__$1,new cljs.core.Keyword(null,"contacts","contacts",333503174),learn_cljs.contacts.replace_contact,idx,contact);
} else {
return cljs.core.update.call(null,state__$1,new cljs.core.Keyword(null,"contacts","contacts",333503174),learn_cljs.contacts.add_contact,contact);
}
})());
});
learn_cljs.contacts.on_cancel_edit = (function learn_cljs$contacts$on_cancel_edit(state){
return learn_cljs.contacts.refresh_BANG_.call(null,cljs.core.dissoc.call(null,state,new cljs.core.Keyword(null,"selected","selected",574897764),new cljs.core.Keyword(null,"editing?","editing?",1646440800)));
});
learn_cljs.contacts.on_open_contact = (function learn_cljs$contacts$on_open_contact(e,state){
return learn_cljs.contacts.refresh_BANG_.call(null,(function (){var idx = (e.currentTarget.dataset.idx | (0));
return cljs.core.assoc.call(null,state,new cljs.core.Keyword(null,"selected","selected",574897764),idx,new cljs.core.Keyword(null,"editing?","editing?",1646440800),true);
})());
});
learn_cljs.contacts.on_delete_contact = (function learn_cljs$contacts$on_delete_contact(e,state){
e.stopPropagation();

var idx = (e.currentTarget.dataset.idx | (0));
return learn_cljs.contacts.refresh_BANG_.call(null,(function (){var G__16757 = cljs.core.update.call(null,state,new cljs.core.Keyword(null,"contacts","contacts",333503174),learn_cljs.contacts.remove_contact,idx);
if(cljs.core._EQ_.call(null,idx,new cljs.core.Keyword(null,"selected","selected",574897764).cljs$core$IFn$_invoke$arity$1(state))){
return cljs.core.dissoc.call(null,G__16757,new cljs.core.Keyword(null,"selected","selected",574897764),new cljs.core.Keyword(null,"editing?","editing?",1646440800));
} else {
return G__16757;
}
})());
});
learn_cljs.contacts.attach_event_handlers_BANG_ = (function learn_cljs$contacts$attach_event_handlers_BANG_(state){
var temp__5823__auto___16766 = goog.dom.getElement("add-contact");
if(cljs.core.truth_(temp__5823__auto___16766)){
var add_button_16767 = temp__5823__auto___16766;
goog.events.listen(add_button_16767,"click",(function (_){
return learn_cljs.contacts.on_add_contact.call(null,state);
}));
} else {
}

var temp__5823__auto___16768 = goog.dom.getElement("save-contact");
if(cljs.core.truth_(temp__5823__auto___16768)){
var save_button_16769 = temp__5823__auto___16768;
goog.events.listen(save_button_16769,"click",(function (_){
return learn_cljs.contacts.on_save_contact.call(null,state);
}));
} else {
}

var temp__5823__auto___16770 = goog.dom.getElement("cancel-edit");
if(cljs.core.truth_(temp__5823__auto___16770)){
var cancel_button_16771 = temp__5823__auto___16770;
goog.events.listen(cancel_button_16771,"click",(function (_){
return learn_cljs.contacts.on_cancel_edit.call(null,state);
}));
} else {
}

var seq__16758_16772 = cljs.core.seq.call(null,cljs.core.array_seq.call(null,goog.dom.getElementsByClass("contact-summary")));
var chunk__16759_16773 = null;
var count__16760_16774 = (0);
var i__16761_16775 = (0);
while(true){
if((i__16761_16775 < count__16760_16774)){
var elem_16776 = cljs.core._nth.call(null,chunk__16759_16773,i__16761_16775);
goog.events.listen(elem_16776,"click",((function (seq__16758_16772,chunk__16759_16773,count__16760_16774,i__16761_16775,elem_16776){
return (function (e){
return learn_cljs.contacts.on_open_contact.call(null,e,state);
});})(seq__16758_16772,chunk__16759_16773,count__16760_16774,i__16761_16775,elem_16776))
);


var G__16777 = seq__16758_16772;
var G__16778 = chunk__16759_16773;
var G__16779 = count__16760_16774;
var G__16780 = (i__16761_16775 + (1));
seq__16758_16772 = G__16777;
chunk__16759_16773 = G__16778;
count__16760_16774 = G__16779;
i__16761_16775 = G__16780;
continue;
} else {
var temp__5823__auto___16781 = cljs.core.seq.call(null,seq__16758_16772);
if(temp__5823__auto___16781){
var seq__16758_16782__$1 = temp__5823__auto___16781;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__16758_16782__$1)){
var c__5525__auto___16783 = cljs.core.chunk_first.call(null,seq__16758_16782__$1);
var G__16784 = cljs.core.chunk_rest.call(null,seq__16758_16782__$1);
var G__16785 = c__5525__auto___16783;
var G__16786 = cljs.core.count.call(null,c__5525__auto___16783);
var G__16787 = (0);
seq__16758_16772 = G__16784;
chunk__16759_16773 = G__16785;
count__16760_16774 = G__16786;
i__16761_16775 = G__16787;
continue;
} else {
var elem_16788 = cljs.core.first.call(null,seq__16758_16782__$1);
goog.events.listen(elem_16788,"click",((function (seq__16758_16772,chunk__16759_16773,count__16760_16774,i__16761_16775,elem_16788,seq__16758_16782__$1,temp__5823__auto___16781){
return (function (e){
return learn_cljs.contacts.on_open_contact.call(null,e,state);
});})(seq__16758_16772,chunk__16759_16773,count__16760_16774,i__16761_16775,elem_16788,seq__16758_16782__$1,temp__5823__auto___16781))
);


var G__16789 = cljs.core.next.call(null,seq__16758_16782__$1);
var G__16790 = null;
var G__16791 = (0);
var G__16792 = (0);
seq__16758_16772 = G__16789;
chunk__16759_16773 = G__16790;
count__16760_16774 = G__16791;
i__16761_16775 = G__16792;
continue;
}
} else {
}
}
break;
}

var seq__16762 = cljs.core.seq.call(null,cljs.core.array_seq.call(null,goog.dom.getElementsByClass("delete-icon")));
var chunk__16763 = null;
var count__16764 = (0);
var i__16765 = (0);
while(true){
if((i__16765 < count__16764)){
var elem = cljs.core._nth.call(null,chunk__16763,i__16765);
goog.events.listen(elem,"click",((function (seq__16762,chunk__16763,count__16764,i__16765,elem){
return (function (e){
return learn_cljs.contacts.on_delete_contact.call(null,e,state);
});})(seq__16762,chunk__16763,count__16764,i__16765,elem))
);


var G__16793 = seq__16762;
var G__16794 = chunk__16763;
var G__16795 = count__16764;
var G__16796 = (i__16765 + (1));
seq__16762 = G__16793;
chunk__16763 = G__16794;
count__16764 = G__16795;
i__16765 = G__16796;
continue;
} else {
var temp__5823__auto__ = cljs.core.seq.call(null,seq__16762);
if(temp__5823__auto__){
var seq__16762__$1 = temp__5823__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__16762__$1)){
var c__5525__auto__ = cljs.core.chunk_first.call(null,seq__16762__$1);
var G__16797 = cljs.core.chunk_rest.call(null,seq__16762__$1);
var G__16798 = c__5525__auto__;
var G__16799 = cljs.core.count.call(null,c__5525__auto__);
var G__16800 = (0);
seq__16762 = G__16797;
chunk__16763 = G__16798;
count__16764 = G__16799;
i__16765 = G__16800;
continue;
} else {
var elem = cljs.core.first.call(null,seq__16762__$1);
goog.events.listen(elem,"click",((function (seq__16762,chunk__16763,count__16764,i__16765,elem,seq__16762__$1,temp__5823__auto__){
return (function (e){
return learn_cljs.contacts.on_delete_contact.call(null,e,state);
});})(seq__16762,chunk__16763,count__16764,i__16765,elem,seq__16762__$1,temp__5823__auto__))
);


var G__16801 = cljs.core.next.call(null,seq__16762__$1);
var G__16802 = null;
var G__16803 = (0);
var G__16804 = (0);
seq__16762 = G__16801;
chunk__16763 = G__16802;
count__16764 = G__16803;
i__16765 = G__16804;
continue;
}
} else {
return null;
}
}
break;
}
});
learn_cljs.contacts.action_button = (function learn_cljs$contacts$action_button(id,text,icon_class){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"button","button",1456579943),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"id","id",-1388402092),id,new cljs.core.Keyword(null,"class","class",-2030961996),"button is-primary is-light"], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"span","span",1394872991),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),["mu ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(icon_class)].join('')], null)], null),[" ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(text)].join('')], null);
});
learn_cljs.contacts.save_button = learn_cljs.contacts.action_button.call(null,"save-contact","Save","mu-file");
learn_cljs.contacts.cancel_button = learn_cljs.contacts.action_button.call(null,"cancel-edit","Cancel","mu-cancel");
learn_cljs.contacts.add_button = learn_cljs.contacts.action_button.call(null,"add-contact","Add","mu-plus");
learn_cljs.contacts.section_header = (function learn_cljs$contacts$section_header(editing_QMARK_){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"section-header"], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level-left"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level-item"], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"h1","h1",-1896887462),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"subtitle"], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"span","span",1394872991),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"mu mu-user"], null)], null),"Edit Contact"], null)], null)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level-right"], null),(cljs.core.truth_(editing_QMARK_)?new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"buttons"], null),learn_cljs.contacts.cancel_button,learn_cljs.contacts.save_button], null):learn_cljs.contacts.add_button)], null)], null)], null);
});
learn_cljs.contacts.format_name = (function learn_cljs$contacts$format_name(contact){
return clojure.string.join.call(null," ",cljs.core.juxt.call(null,new cljs.core.Keyword(null,"first-name","first-name",-1559982131),new cljs.core.Keyword(null,"last-name","last-name",-1695738974)).call(null,contact));
});
learn_cljs.contacts.delete_icon = (function learn_cljs$contacts$delete_icon(idx){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"span","span",1394872991),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"class","class",-2030961996),"delete-icon",new cljs.core.Keyword(null,"data-idx","data-idx",1803716006),idx], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"span","span",1394872991),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"mu mu-delete"], null)], null)], null);
});
learn_cljs.contacts.render_contact_list_item = (function learn_cljs$contacts$render_contact_list_item(idx,contact,selected_QMARK_){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"class","class",-2030961996),["card contact-summary",(cljs.core.truth_(selected_QMARK_)?" selected":null)].join(''),new cljs.core.Keyword(null,"data-idx","data-idx",1803716006),idx], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"card-content"], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level-left"], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level-item"], null),learn_cljs.contacts.delete_icon.call(null,idx),learn_cljs.contacts.format_name.call(null,contact)], null)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"level-right"], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"span","span",1394872991),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"mu mu-right"], null)], null)], null)], null)], null)], null);
});
learn_cljs.contacts.render_contact_list = (function learn_cljs$contacts$render_contact_list(state){
var contacts = new cljs.core.Keyword(null,"contacts","contacts",333503174).cljs$core$IFn$_invoke$arity$1(state);
var selected = new cljs.core.Keyword(null,"selected","selected",574897764).cljs$core$IFn$_invoke$arity$1(state);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"contact-list column is-4 hero"], null),cljs.core.map_indexed.call(null,(function (idx,contact){
return learn_cljs.contacts.render_contact_list_item.call(null,idx,contact,cljs.core._EQ_.call(null,idx,selected));
}),contacts)], null);
});
learn_cljs.contacts.no_contact_details = new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"p","p",151049309),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"notice"], null),"No contact selected"], null);
learn_cljs.contacts.form_field = (function learn_cljs$contacts$form_field(var_args){
var G__16806 = arguments.length;
switch (G__16806) {
case 3:
return learn_cljs.contacts.form_field.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return learn_cljs.contacts.form_field.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error(["Invalid arity: ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(arguments.length)].join('')));

}
});

(learn_cljs.contacts.form_field.cljs$core$IFn$_invoke$arity$3 = (function (id,value,label){
return learn_cljs.contacts.form_field.call(null,id,value,label,"text");
}));

(learn_cljs.contacts.form_field.cljs$core$IFn$_invoke$arity$4 = (function (id,value,label,type){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"field"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"label","label",1718410804),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"label"], null),label], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"control"], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"input","input",556931961),new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"id","id",-1388402092),id,new cljs.core.Keyword(null,"value","value",305978217),value,new cljs.core.Keyword(null,"type","type",1174270348),type,new cljs.core.Keyword(null,"class","class",-2030961996),"input"], null)], null)], null)], null);
}));

(learn_cljs.contacts.form_field.cljs$lang$maxFixedArity = 4);

learn_cljs.contacts.render_contact_details = (function learn_cljs$contacts$render_contact_details(contact){
var address = cljs.core.get.call(null,contact,new cljs.core.Keyword(null,"address","address",559499426),cljs.core.PersistentArrayMap.EMPTY);
return new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"id","id",-1388402092),"contact-form",new cljs.core.Keyword(null,"class","class",-2030961996),"contact-form"], null),learn_cljs.contacts.form_field.call(null,"input-first-name",new cljs.core.Keyword(null,"first-name","first-name",-1559982131).cljs$core$IFn$_invoke$arity$1(contact),"First Name"),learn_cljs.contacts.form_field.call(null,"input-last-name",new cljs.core.Keyword(null,"last-name","last-name",-1695738974).cljs$core$IFn$_invoke$arity$1(contact),"Last Name"),learn_cljs.contacts.form_field.call(null,"input-email",new cljs.core.Keyword(null,"email","email",1415816706).cljs$core$IFn$_invoke$arity$1(contact),"Email","email"),new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"fieldset","fieldset",-1949770816),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"legend","legend",-1027192245),"Address"], null),learn_cljs.contacts.form_field.call(null,"input-street",new cljs.core.Keyword(null,"street","street",1870012303).cljs$core$IFn$_invoke$arity$1(address),"Street"),learn_cljs.contacts.form_field.call(null,"input-city",new cljs.core.Keyword(null,"city","city",-393302614).cljs$core$IFn$_invoke$arity$1(address),"City"),learn_cljs.contacts.form_field.call(null,"input-state",new cljs.core.Keyword(null,"state","state",-1988618099).cljs$core$IFn$_invoke$arity$1(address),"State"),learn_cljs.contacts.form_field.call(null,"input-postal",new cljs.core.Keyword(null,"postal","postal",-1824232834).cljs$core$IFn$_invoke$arity$1(address),"Postal Code"),learn_cljs.contacts.form_field.call(null,"input-country",new cljs.core.Keyword(null,"country","country",312965309).cljs$core$IFn$_invoke$arity$1(address),"Country")], null)], null);
});
learn_cljs.contacts.top_bar = new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"navbar has-shadow"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"container"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"navbar-brand"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"span","span",1394872991),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"navbar-item"], null),"ClojureScript Contacts"], null)], null)], null)], null);
learn_cljs.contacts.render_app_BANG_ = (function learn_cljs$contacts$render_app_BANG_(state){
return learn_cljs.contacts.set_app_html_BANG_.call(null,["<div class=\"app-main\"></div>",cljs.core.str.cljs$core$IFn$_invoke$arity$1(hiccups.runtime.render_html.call(null,learn_cljs.contacts.top_bar)),"<div class=\"columns\"></div>",cljs.core.str.cljs$core$IFn$_invoke$arity$1(hiccups.runtime.render_html.call(null,learn_cljs.contacts.render_contact_list.call(null,state))),"<div class=\"contact-details column is-8\"></div>",cljs.core.str.cljs$core$IFn$_invoke$arity$1(hiccups.runtime.render_html.call(null,learn_cljs.contacts.section_header.call(null,new cljs.core.Keyword(null,"editing?","editing?",1646440800).cljs$core$IFn$_invoke$arity$1(state)))),"<div class=\"hero\"></div>",(cljs.core.truth_(new cljs.core.Keyword(null,"editing?","editing?",1646440800).cljs$core$IFn$_invoke$arity$1(state))?cljs.core.str.cljs$core$IFn$_invoke$arity$1(hiccups.runtime.render_html.call(null,learn_cljs.contacts.render_contact_details.call(null,cljs.core.get_in.call(null,state,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"contacts","contacts",333503174),new cljs.core.Keyword(null,"selected","selected",574897764).cljs$core$IFn$_invoke$arity$1(state)], null),cljs.core.PersistentArrayMap.EMPTY)))):cljs.core.str.cljs$core$IFn$_invoke$arity$1(hiccups.runtime.render_html.call(null,learn_cljs.contacts.no_contact_details)))].join(''));
});
learn_cljs.contacts.refresh_BANG_ = (function learn_cljs$contacts$refresh_BANG_(state){
learn_cljs.contacts.render_app_BANG_.call(null,state);

return learn_cljs.contacts.attach_event_handlers_BANG_.call(null,state);
});
learn_cljs.contacts.refresh_BANG_.call(null,learn_cljs.contacts.initial_state);

//# sourceMappingURL=contacts.js.map
