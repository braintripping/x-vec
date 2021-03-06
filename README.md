## X-Expressions in Clojure, with vectors

### Background

The most popular format for specifying HTML in Clojure is [Hiccup](https://github.com/weavejester/hiccup), by [James Reeves](https://www.booleanknot.com/). It has spawned variants like [sablono](https://github.com/r0man/sablono) and [reagent](https://github.com/reagent-project/reagent) which return React components rather than HTML strings.

This summer I read Matthew Butterick's explanation of [X-Expressions](http://docs.racket-lang.org/pollen/second-tutorial.html#%28part._.X-expressions%29), then discovered a related [paper](https://www.cs.colorado.edu/~ralex/papers/PDF/X-expressions.pdf) and additional [references](https://i.imgur.com/LxiNuEq.png) via Jack Rusher. My intuition about what `hiccup` is began to shift from "a way to generate HTML in Clojure" to "an implementation of X-Expressions in Clojure using vectors".

This library, `x-vec`, is a small toolset for building 'emitters' that output different kinds of useful things from X-Vector notation. For example:

- React components (just like Sablono/Reagent)
- GraphQL queries
- Code ASTs

Having previously written a [hiccup variant](https://github.com/braintripping/re-view/blob/master/re_view/hiccup.md) myself, I used that code as well as some prior drafts for a graphql emitter as the starting point for this repo.

----

### References

- Matthew Butterick's description of X-Expressions: http://docs.racket-lang.org/pollen/second-tutorial.html#%28part._.X-expressions%29
- The paper, [X-expressions in XMLisp: S-expressions and Extensible Markup Language Unite](https://www.cs.colorado.edu/~ralex/papers/PDF/X-expressions.pdf)
- [The Next 700 Markup Languages](http://homepages.inf.ed.ac.uk/wadler/papers/next700/next700.pdf)
    ![](https://i.imgur.com/LxiNuEq.png)
- The very [first README](https://github.com/weavejester/hiccup/blob/0823544a735f034b8273391e1416e98e6d910ead/README.markdown) for @weavejester's [Hiccup](https://github.com/weavejester/hiccup) library    