# Patent Reaction Extractor

A presentation on this software is available [here](https://www.slideshare.net/dan2097/automated-extraction-of-reactions-from-the-patent-literature)

Reactions extracted using this software, in collaboration with [NextMove Software](https://www.nextmovesoftware.com/), covering US patents from 1976 to September 2016 are freely available [here](https://figshare.com/articles/Chemical_reactions_from_US_patents_1976-Sep2016_/5104873)

NextMove Software commercially provides an up to date database of automatically extracted reactions as part of their [Pistachio](https://www.nextmovesoftware.com/pistachio.html) product.

Older results from this software are available [here](https://figshare.com/articles/Legacy_reaction_extraction_data_1976-2013_/12084729)

This software is licensed under the GPLv3 for compatibility with Epam's [Indigo](https://lifescience.opensource.epam.com/indigo/) toolkit

---

# Instructions for use

The system takes as input either an XML patent (recent USPTO and EPO patents tested as working) or 
a list of "heading" and "p" elements in the order they appear in a document.

For the former use case, where inputStream is an inputStream from an XML patent:

```
Document doc = Utils.buildXmlFile(inputStream);
ReactionExtractor extractor = new ReactionExtractor(doc);
extractor.extractReactions();
Map<Reaction, IndigoObject> completeReactions = extractor.getAllCompleteReactions();
```

completeReactions are those for which an atom map that accounts for the origins of all atoms in the product/s could be accounted for.
The returned map contains associated Reaction objects which can be inspected or trivially serialised to CML via their `toCML()` method.
The IndigoObjects are Indigo reactions (created by the Indigo toolkit) that contains the unique structure resolvable components from the Reaction objects.
They can be inspected to retrieve the atom mapping Indigo assigned.

`Utils.serializeReactions(outputDir, completeReactions)` is a useful convenience method for serialising reactions to CML and graphical depictions

---
# Advanced Usage

In the presentation, precision was enhanced by restricting the reactions to those that had no reactants/spectators/products with a ChemicalEntityType of chemicalClass or fragment
Additionally all products were required to have been associated with a chemical structure (can be checked with `hasInchi()` and `hasSmiles()`)

ExtractOrganicChemistryPatents may be used filter patents downloaded from Google (http://www.google.com/googlebooks/uspto-patents.html) to just organic chemistry patents.

Performance can be adjusted by using `extractor.setIndigoAtomMappingTimeout`

This sets how long, at maximum, may be spent atom mapping a reaction
