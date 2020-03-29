create table directory (
	id INTEGER PRIMARY KEY autoincrement,
	canonical_path text
);


create table archive (
	id INTEGER PRIMARY KEY autoincrement,
	name text,
	length integer,
	last_modified datetime,
	partial_hash text,
	full_hash text,
	directory_id integer);

CREATE INDEX idx_archive_dirid ON archive(directory_id);
CREATE INDEX idx_archive_phash ON archive(partial_hash);
