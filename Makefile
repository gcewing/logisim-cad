ICONS_SRCDIR := src/main/resources/resources/logisim/img
ICONS_FILENAME := Logisim-evolution
ICON_SRCFILE := logisim-icon-128.png

all:
	@echo 'Targets: jar run app icons'

jar:
	./gradlew shadowJar

run:
	java -jar build/libs/logisim-evolution-*.jar

app:
	./gradlew createApp

icons: iconset icns

iconset:
	cd $(ICONS_SRCDIR); \
	mkdir -p $(ICONS_FILENAME).iconset; \
	for size in 16 32 64 128 256 512; do \
		size2=$$((2 * size)); \
		sips -z $$size $$size $(ICON_SRCFILE) --out $(ICONS_FILENAME).iconset/icon_$${size}x$${size}.png; \
		sips -z $$size2 $$size2 $(ICON_SRCFILE) --out $(ICONS_FILENAME).iconset/icon_$${size}x$${size}@2x.png; \
	done

icns:
	cd $(ICONS_SRCDIR); \
	iconutil -c icns $(ICONS_FILENAME).iconset
