// Import vocabulary
LOAD CSV WITH HEADERS FROM "file:///cdmh/Vocabulary.csv" AS row
CREATE (a:Vocabulary { name: row.NAME, cdmh_id: row.VOCABULARY_ID });

// import code
USING PERIODIC COMMIT 500
LOAD CSV WITH HEADERS FROM "file:///cdmh/Code_Values_ALL.csv" AS row
CREATE (a:Code { value: row.CODE, ncit_code: row.NCIT_CONCEPT_CODE, short_name: row.SHORT_NAME, long_name: row.LONG_NAME, description: row.DESCRIPTION, cdmh_id: row.CODE_VALUE_ID });

// import code-vocabulary relationship
USING PERIODIC COMMIT 500
LOAD CSV WITH HEADERS FROM "file:///cdmh/Code_Values_ALL.csv" AS row
MATCH (a:Code), (b:Vocabulary)
WHERE a.cdmh_id=row.CODE_VALUE_ID and b.cdmh_id=row.VOCABULARY_ID
CREATE (a)-[r:PART_OF]->(b);

// import parent relationship
USING PERIODIC COMMIT 500
LOAD CSV WITH HEADERS FROM "file:///cdmh/Code_Values_ALL.csv" AS row
MATCH (a:Code), (b:Code)
WHERE a.cdmh_id=row.CODE_VALUE_ID and b.cdmh_id=row.PARENT_CODE_VALUE_ID
CREATE (a)-[r:HAS_PARENT]->(b);

// import mapping
USING PERIODIC COMMIT 500
LOAD CSV WITH HEADERS FROM "file:///cdmh/Code_Mapping.csv" AS row
MATCH (a:Code), (b:Code)
WHERE a.cdmh_id=row.FROM_CODE_VALUE_ID and b.cdmh_id=row.TO_CODE_VALUE_ID
CREATE (a)-[r:MAP_TO]->(b);



