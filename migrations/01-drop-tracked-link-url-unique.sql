-- Allow the same URL to be tracked by different chats
ALTER TABLE tracked_link DROP CONSTRAINT IF EXISTS tracked_link_url_key;
