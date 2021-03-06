![welcome](https://raw.github.com/super-saubaer/PrintableMusic/master/Resources/PRINTABLE_MUSIC/welcome_neu.jpg)


Projekt im Seminar Künstlerisch/Mediales Projekt, Sommers Semester 2015, Ludwig-Maximilians Universität München.
Sebastian Siepe, Masterstudiengang Medininformatik.


# PrintableMusic
PrintableMusic ist ein künstlerisches Projeekt, welches das Ziel verfolgt, Musik zum Anfassen zu gestalten.
Musik ist etwas flüchtiges: Wir können Musik erleben, solange diese erklingt und wir sie hören können. Dieses Erlebnis der Musik ist jedoch vorbei, sobald sie nicht mehr zu hören ist. Es liegt in der Natur der Musik, das sie nicht zu betrachten, oder anzufassen, sondern nur zu hören ist.

Bei diesem Gedanken setzt PrintableMusic an. Denn PrintableMusic versucht Musik eine neue Form zu geben, so dass sie betrachtet, erfühlt und physisch verglichen werden kann. 
Aus diesem Grund wird wandelt PrintableMusic Musik in künstlerische Skulpturen. Diese Skulpturen können anschließend durch einen 3D-Drucker ausgedruckt werden. Durch dieses Vorgehen wird aus Musik ein physisches Objekt, welches die eingespielte Musik räpresentiert. 

Die Skulpturen bieten nun die Möglichkeit, Musik visuell zu betrachten, haptisch zu erfühlen und anhand verschiedener Eigenschaften wie Größe, Form, Ausprägung, Gewicht und Farbe zu vergleichen.

# Funktionsweise
PrintableMusic ist entworfen worden, um Live-eingespielte Musik in physikalische Skulpturen zu wandeln. Als Austauschformat wurde MIDI gewählt. Das Programm ist dementsprechend in der Lage, MIDI-Signale eines Musikinstrumentes entgegenzunehmen, beispielsweise die MIDI-Informationen eines Keyboards. Diese Signale werden durch einen Synthesizer in Töne gewandelt und anschließend wiedergegeben. Der User hört also, was er spielt. 
Die MIDI-Informationen werden nachfolgend in abstrakte 3D-Objekte gewandelt, welche die Skulpturen darstellen. Dabei erweitert jeder Ton die Skulptur um ein weiteres, kleines Element, je nach Informationsgehalt der Tonhöhe und Lautstärke. 
Dieses 3D-Objekt wird anschließend in eine druckbare SCAD-Datei exportiert, sodass die Form durch einen 3D-Drucker ausgedruckt werden kann.

# Bedienung
PrintableMusic besitzt ein User Interfaces, welches die Bedienung des Programmes ermöglicht.

<img src="https://raw.github.com/super-saubaer/PrintableMusic/master/Resources/bilder/main.png" width="250" align="center"/>
<img src="https://raw.github.com/super-saubaer/PrintableMusic/master/Resources/bilder/start.png" width="250" align="center"/>
<img src="https://raw.github.com/super-saubaer/PrintableMusic/master/Resources/bilder/stop.png" width="250" align="center"/>

Zunächst muss eine der beiden Skulpturen-Arten ausgewählt werden. Wird 'Cubes' ausgewählt, so wird die Skulptur aus unterschiedlchen kleinen Würfeln zusammengestellt, die sich in Größe, Position und Lage unterscheiden. 'Prisms' erzeugt eine Skulptur, die aus unterschiedlichen Platten besteht, die nacheinander aufeinandergereiht werden.
Ist die Skulpturen-Art ausgewählt, kann unter dem Zweiten Punkt die Aufnahme gestartet werden. Ab jetzt werden alle MIDI-Informationen gespeichert und in echtzeit zur Skultpur hinzugefügt. Durch anklicken des 'Safe Sculpture' Buttons, wird die druckbare SCAD-Datei gespeichert und diese kann nun an einen 3D-Drucker weitergeleitet werden. 


# Darstellung
Die nun erstellten SCAD-Dateien können mit dem open-source Programm [openSCAD](http://www.openscad.org/) dargestellt werden. Da openSCAD automatisch die Darstellung anpasst, sobald sich die Informationen einer Datei ändern, kann durch openScad auch die Entstehung der Skulptur live beim Musik spielen angezeigt werden. Der Benutzer sieht also, wie sich die Skulptur nach und nach entsteht. 

![current](https://raw.github.com/super-saubaer/PrintableMusic/master/Resources/bilder/current.png)
