LogisimE
========

This is a fork of logisim-evolution with some features making it easier
to use for designing circuits to be built with real parts. amd to help
produce nice-looking schematics.

Improvements to the Appearance Editor
-------------------------------------

### Improved snapping of text objects

When dragging a text object with snapping, the point on
the object that is snapped to the grid depends on the alignment attributes of
the text. This makes it easier to position the text nicely in relation to
wire attachment points and other features of your circuit symbol.

Text objects also have a Margin attribute that leaves a horizontal offset
between the text and the grid point that it snaps to. This allows you to
easily position text next to a box with suitable spacing.

These features are backwards-compatible -- if a circuit that uses them is
loaded into a standard version of Logisim-evolution, text objects will
still appear in the same place.

### Finer snapping grid

The snapping grid for all objects except ports
and the origin marker is half the size of the wire grid. This makes it
easy, for example, to line up an inversion circle with a wire properly.

This feature is backwards-compatible.

### Option to snap by default

There is an application preference to reverse the action of the Control key,
so that snapping is the default, and holding Control prevents snapping.

This feature is backwards-compatible.

### Circular arcs

The Curve object has an option to render it as a circular or elliptical arc
instead of a quadratic bezier curve.

This feature is partially backwards-compatible. Standard Logisim-evolution will
render the arc as a quadratic bezier having the same control points, which will
look approximately right but a bit wonky.
