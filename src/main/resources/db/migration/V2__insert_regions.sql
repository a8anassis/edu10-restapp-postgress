INSERT INTO regions (id, name) VALUES
(1,  'ΑΝΑΤΟΛΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ ΚΑΙ ΘΡΑΚΗΣ'),
(2,  'ΑΤΤΙΚΗΣ'),
(3,  'ΒΟΡΕΙΟΥ ΑΙΓΑΙΟΥ'),
(4,  'ΔΥΤΙΚΗΣ ΕΛΛΑΔΑΣ'),
(5,  'ΔΥΤΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ'),
(6,  'ΗΠΕΙΡΟΥ'),
(7,  'ΘΕΣΣΑΛΙΑΣ'),
(8,  'ΙΟΝΙΩΝ ΝΗΣΩΝ'),
(9,  'ΚΕΝΤΡΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ'),
(10, 'ΚΡΗΤΗΣ'),
(11, 'ΝΟΤΙΟΥ ΑΙΓΑΙΟΥ'),
(12, 'ΠΕΛΟΠΟΝΝΗΣΟΥ'),
(13, 'ΣΤΕΡΕΑΣ ΕΛΛΑΔΑΣ');

-- Advance the identity sequence past the explicitly inserted IDs
ALTER TABLE regions ALTER COLUMN id RESTART WITH 14;