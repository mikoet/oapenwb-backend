-- Q010
-- Find all SynGroups that are connected to at least one LexemeForm that contains the searched text
-- (via the variants, lexemes and sememes).
select sg.id as id, sg.description as description, sg.presentation as presentation
from SynGroups sg, Sememes se left join Variants Va on (se.lexemeID = Va.lexemeID and Va.mainVariant=true)
where sg.sememeIDs @> ('[' || se.id || ']')::jsonb -- attention: escape the colons
  and se.lexemeID in (
    select L.id
    from Lexemes L left join Variants V on (L.id = V.lexemeID and V.mainVariant=true)
    where L.langID = :langID
	  and L.typeID = :typeID -- optional part!
      and L.id in (select lexemeID from Variants Vi
      where Vi.id in (select variantID from LexemeForms
        where @@@filterStatement@@@ -- searchableText @@ websearch_to_tsquery('simple', :filter)
                                    -- searchableText @@ to_tsquery('simple', :filter)
)))
order by Va.main
limit :limit offset :offset

-- Q011
-- Find all Lexemes that are connected to at least one LexemeForm that contains the searched text
-- (via the variants, lexemes and sememes).
select L.id as id, L.parserID as parserID, L.typeID as typeID, L.langID as langID,
  V.pre as pre, V.main as main, V.post as post, L.active as active,
  5 as condition, L.tags as tags
from Lexemes L left join Variants V on (L.id = V.lexemeID and V.mainVariant=true)
where
  L.id in (select lexemeID from Variants Vi
    where Vi.id in (select variantID from LexemeForms where @@@filterStatement@@@)) -- searchableText @@ websearch_to_tsquery('simple', :filter)
                                                                                    -- searchableText @@ to_tsquery('simple', :filter)
  and L.langID = :langID
  and L.typeID = :typeID -- optional!
order by V.main
limit :limit offset :offset

-- Q012 ?

-- Q020
-- Get the lemmas for the generation of the presentation for a SynGroup.
select Va.pre, Va.main, Va.post
from Sememes Se left join Variants Va on (se.lexemeID = Va.lexemeID)
where Se.variantIDs @> ('[' || Va.id || ']')::jsonb -- attention: escape the colons
  and Se.id = :id
order by Va.main

-- Q030
-- Get a slim sememe
select S.id as id, S.internalName as internalName, S.active as active, S.spec as spec,
  S.lexemeID as lexemeID, L.typeID as typeID, L.langID as langID, L.active as lexActive, V.pre as pre,
  V.main as main, V.post as post
from Sememes S inner join Lexemes L on S.lexemeID = L.id
  inner join Variants V on (L.id = V.lexemeID and V.mainVariant)
where S.id = :sememeID

-- Q900
-- FileImporter / Existence Checker -> lexemeExists (simple check)
select 1 from Lexemes l
  inner join Variants v on l.id = v.lexemeID
  where v.mainVariant and v.main = :lemma
    and l.typeID = :typeID and l.langID = :langID;