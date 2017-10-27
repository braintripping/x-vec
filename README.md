# x-vec

X-Expressions in Clojure, with vectors

**Motivation and Approach**

The most popular format for specifying HTML in Clojure is [Hiccup](https://github.com/weavejester/hiccup), by [James Reeves](https://www.booleanknot.com/). It has spawned some variants, [sablono](https://github.com/r0man/sablono) and [reagent](https://github.com/reagent-project/reagent) being the most common, which can return React components rather than HTML strings. 

While developing [re-view](https://github.com/braintripping/re-view) I ran into limitations with existing libraries and so had to write my own [hiccup variant](https://github.com/braintripping/re-view/blob/master/re_view/hiccup.md). I needed more flexibility with respect to resolution of keywords to functions, and the ability to dynamically modify element attributes.

In the summer of 2017 I read Matthew Butterick's explanation of [X-Expressions](http://docs.racket-lang.org/pollen/second-tutorial.html#%28part._.X-expressions%29), and then discovered the X-Expression [paper](https://www.cs.colorado.edu/~ralex/papers/PDF/X-expressions.pdf) and some additional [references](https://i.imgur.com/LxiNuEq.png) from Jack Rusher. This changed my intuition about `hiccup`. Instead of being "a way to generate HTML in Clojure", I saw it as an implementation of X-Expressions in Clojure using vectors, which could have applications beyond HTML. 

My current hypothesis is that a general `X-Expression` library in Clojure, not tied to any particular output format, could be a useful thing. Instead of using the list notation of `X-Expressions`, I prefer the vector notation of Hiccup, and so would call these expressionso `X-Vectors`.

### Proposed use cases

The three use cases I currently have in mind for X-Vectors are:

- Replace the implementation of `re-view.hiccup` with `x-vec`. (in fact, code from `re-view.hiccup` will form the initial seed of `x-vec`.)
- Write GraphQL queries in X-Vectors (see the sketch: https://dev.maria.cloud/gist/f7da365706c7ed8b7b77ac042f0f4a36)
- Use X-Vectors for the AST nodes of my whitespace-aware Clojure parsing library, previously [magic-tree](https://github.com/braintripping/magic-tree) and now [lark/tree](https://github.com/braintripping/lark/tree/master/tools/src/lark/tree).

----

### References

- Matthew Butterick's description of X-Expressions: http://docs.racket-lang.org/pollen/second-tutorial.html#%28part._.X-expressions%29
- The paper, [X-expressions in XMLisp: S-expressions and Extensible Markup Language Unite](https://www.cs.colorado.edu/~ralex/papers/PDF/X-expressions.pdf)
- [The Next 700 Markup Languages](http://homepages.inf.ed.ac.uk/wadler/papers/next700/next700.pdf)
    ![](https://i.imgur.com/LxiNuEq.png)
- The very [first README](https://github.com/weavejester/hiccup/blob/0823544a735f034b8273391e1416e98e6d910ead/README.markdown) for @weavejester's [Hiccup](https://github.com/weavejester/hiccup) library    
