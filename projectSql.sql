CREATE TABLE file_id	(
	path_to_file  varchar(20) NOT NULL,
	id INT NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (id)
);

CREATE TABLE cells	(
	row_column varchar(10),
	content varchar(256),
	id INT,
	style_flags varchar(2),
	FOREIGN KEY (id) references file_id(id)
		ON DELETE CASCADE
);