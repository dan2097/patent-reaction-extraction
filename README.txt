Patent Reaction Extractor (v1.0)
Daniel Lowe (daniel@nextmovesoftware.com)
https://bitbucket.org/dan2097/patent-reaction-extraction

A presentation on this software is available at:
http://www.slideshare.net/dan2097/automated-extraction-of-reactions-from-the-patent-literature

This software is licensed under the GPLv3 for compatibility to allow compatibility with GGA's Indigo toolkit (http://ggasoftware.com/opensource/indigo)

##################################################

Instructions for use:

The system takes as input either an XML patent (recent USPTO and EPO patents tested as working) or 
a list of "heading" and "p" elements in the order they appear in a document.

For the former use case, where inputStream is an inputStream from an XML patent:

Document doc = Utils.buildXmlFile(inputStream);
ReactionExtractor extractor = new ReactionExtractor(doc);
extractor.extractReactions();
Map<Reaction, IndigoObject> completeReactions = extractor.getAllCompleteReactions();

completeReactions are those for which an atom map that accounts for the origins of all atoms in the product/s could be accounted for.
The returned map contains associated Reaction objects which can be inspected or trivially serialised to CML via their toCML() method.
The IndigoObjects are Indigo reactions (created by the Indigo toolkit) that contains the unique structure resolvable components from the Reaction objects.
They can be inspected to retrieve the atom mapping Indigo assigned.

Utils.serializeReactions(outputDir, completeReactions) is a useful convenience method for serialising reactions to CML and graphical depictions

##################################################
Advanced Usage:

In the presentation, precision was enhanced by restricting the reactions to those that had no reactants/spectators/products with a ChemicalEntityType of chemicalClass or fragment
Additionally all products were required to have been associated with a chemical structure (can be checked with hasInchi() and hasSmiles())

ExtractOrganicChemistryPatents may be used filter patents downloaded from Google (http://www.google.com/googlebooks/uspto-patents.html) to just organic chemistry patents.

Performance can be adjusted by using extractor.setIndigoAtomMappingTimeout
This sets how long, at maximum, may be spent atom mapping a reaction

##################################################

Feedback is always appreciated! (daniel@nextmovesoftware.com)
