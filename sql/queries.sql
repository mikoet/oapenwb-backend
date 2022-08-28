-- Q010
-- Find all SynGroups that are connected to at least one LexemeForm that contains the searched text
-- (via the variants, lexemes and sememes).
select sg.id as id, sg.description as description, sg.presentation as presentation
from SynGroups sg, Sememes se left join Variants Va on (se.lexemeID = Va.lexemeID and Va.mainVariant=true)
where sg.sememeIDs @> ('[' || se.id || ']')::jsonb -- attention: escape the colons -- TODO string concatenation is bad practive here, see query Q500
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
  V.pre as pre, V.main as main, V.post as post, L.active as active, 5 as condition,
  L.tags as tags, S.id as sememeID
from Lexemes L left join Variants V on (L.id = V.lexemeID and V.mainVariant=true)
  left join Sememes S on (L.id = S.lexemeID AND S.id = (SELECT MIN(lexemeID) FROM Sememes WHERE lexemeID = L.id))
where 1 = 1
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


-- Q500
-- Search query for both directions. Part before union is for left-to-right
-- while part after union is for right-to-left search.
select sememeOneID, sememeTwoID, weight
from Mappings
where sememeOneID in (
	select s.id from Sememes s inner join Lexemes l on s.lexemeID = l.id,
		jsonb_array_elements(s.variantIDs) va(variantID)
	where l.langID = :langOneID and variantID::int in ( -- escape the :: (!)
		select variantID from LexemeForms
			where searchableText @@ websearch_to_tsquery('simple', :term)
	)
)
union
select sememeOneID, sememeTwoID, weight
from Mappings
where sememeTwoID in (
	select s.id from Sememes s inner join Lexemes l on s.lexemeID = l.id,
		jsonb_array_elements(s.variantIDs) va(variantID)
	where l.langID = :langTwoID and variantID::int in ( -- escape the :: (!)
		select variantID from LexemeForms
			where searchableText @@ websearch_to_tsquery('simple', :term)
	)
)
order by weight desc


-- Q030
-- Get a slim sememe
select S.id as id, S.internalName as internalName, S.active as active, S.spec as spec,
  S.lexemeID as lexemeID, L.typeID as typeID, L.langID as langID, L.active as lexActive, V.pre as pre,
  V.main as main, V.post as post
from Sememes S inner join Lexemes L on S.lexemeID = L.id
  inner join Variants V on (L.id = V.lexemeID and V.mainVariant)
where S.id = :sememeID

-- Q900 TODO deprecated
-- FileImporter / Existence Checker -> lexemeExists (simple check)
SELECT 1 FROM Lexemes l
  INNER JOIN Variants v ON l.id = v.lexemeID
  where v.mainVariant and v.main = :lemma
    and l.typeID = :typeID and l.langID = :langID;

-- Q901 / T_Lexemes T_Variants
-- FileImporter / Check for existence of a lexeme via the lemma of a variant
SELECT distinct l.id AS lexemeID FROM Lexemes l
  INNER JOIN Variants v ON l.id = v.lexemeID
  INNER JOIN LexemeForms lf ON v.id = lf.variantID
  WHERE l.langID = :langID AND l.typeID = :typeID
    AND lf.text = :text AND lf.formTypeID = :formTypeID
--SELECT distinct l.id AS id FROM Lexemes l
--  INNER JOIN Variants v ON l.id = v.lexemeID
--  WHERE ((:pre is null AND v.pre is null) OR v.pre = cast(:pre AS text))
--    AND v.main = :main
--    AND ((:post is null AND v.post is null) OR v.post = cast(:post AS text))
--    AND ((:also is null AND v.also is null) OR v.also = cast(:also AS text))
