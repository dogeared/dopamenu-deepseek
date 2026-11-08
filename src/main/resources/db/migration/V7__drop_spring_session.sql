-- Spring Session JDBC is incompatible with Vaadin Flow: the VaadinSession is
-- lost when it is deserialized on the next request, so every UIDL request
-- reports "sessionExpired". Sessions are back in the servlet container, so
-- these tables are no longer used.
DROP TABLE IF EXISTS spring_session_attributes;
DROP TABLE IF EXISTS spring_session;
