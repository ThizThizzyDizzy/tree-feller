package com.thizthizzydizzy.treefeller.core.control;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Semantic UI model. Renderers choose their own widgets and input methods. */
public final class Screen{
    public final String title;
    public final List<Entry> entries;
    public Screen(String title,List<Entry> entries){this.title=title;this.entries=Collections.unmodifiableList(new ArrayList<>(entries));}
    public enum Kind{SECTION,VALUE,TOGGLE,ADD,BACK,SAVE,REMOVE,MOVE}
    public static final class Entry{
        public final String title,description;
        public final Runnable activate;
        public final Kind kind;
        public Entry(String title,String description,Runnable activate){this(title,description,activate,Kind.VALUE);}
        public Entry(String title,String description,Runnable activate,Kind kind){this.title=title;this.description=description;this.activate=activate;this.kind=kind;}
    }
}
