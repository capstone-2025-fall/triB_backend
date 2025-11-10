-- Predefined hashtags for Free Board (자유게시판)
-- These hashtags are pre-defined and can be selected by users when creating free board posts

INSERT INTO hashtags (tag_name, tag_type) VALUES ('후기', 'PREDEFINED')
ON DUPLICATE KEY UPDATE tag_name = tag_name;

INSERT INTO hashtags (tag_name, tag_type) VALUES ('맛집', 'PREDEFINED')
ON DUPLICATE KEY UPDATE tag_name = tag_name;

INSERT INTO hashtags (tag_name, tag_type) VALUES ('꿀팁', 'PREDEFINED')
ON DUPLICATE KEY UPDATE tag_name = tag_name;

INSERT INTO hashtags (tag_name, tag_type) VALUES ('자연여행', 'PREDEFINED')
ON DUPLICATE KEY UPDATE tag_name = tag_name;

INSERT INTO hashtags (tag_name, tag_type) VALUES ('도시여행', 'PREDEFINED')
ON DUPLICATE KEY UPDATE tag_name = tag_name;

INSERT INTO hashtags (tag_name, tag_type) VALUES ('주의', 'PREDEFINED')
ON DUPLICATE KEY UPDATE tag_name = tag_name;

INSERT INTO hashtags (tag_name, tag_type) VALUES ('자유', 'PREDEFINED')
ON DUPLICATE KEY UPDATE tag_name = tag_name;
