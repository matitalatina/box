--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.1
-- Dumped by pg_dump version 9.6.1

-- Started on 2017-06-08 11:18:19

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12387)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2150 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 188 (class 1259 OID 24997)
-- Name: field; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE field (
    id integer NOT NULL,
    form_id integer NOT NULL,
    type text NOT NULL,
    key text NOT NULL,
    widget text,
    "refModel" text,
    "refValueProperty" text,
    subform integer,
    "localFields" text,
    "subFields" text,
    "subFilter" text,
    "default" text,
    min integer,
    max integer
);


ALTER TABLE field OWNER TO postgres;

--
-- TOC entry 2151 (class 0 OID 0)
-- Dependencies: 188
-- Name: COLUMN field."subFields"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN field."subFields" IS 'comma separed subform fields to bind with localFields';


--
-- TOC entry 190 (class 1259 OID 25652)
-- Name: field_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE field_i18n (
    id integer NOT NULL,
    field_id integer,
    lang character(2),
    title text,
    placeholder text,
    tooltip text,
    hint text,
    "refTextProperty" text
);


ALTER TABLE field_i18n OWNER TO postgres;

--
-- TOC entry 187 (class 1259 OID 24995)
-- Name: field_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE field_id_seq OWNER TO postgres;

--
-- TOC entry 2152 (class 0 OID 0)
-- Dependencies: 187
-- Name: field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE field_id_seq OWNED BY field.id;


--
-- TOC entry 186 (class 1259 OID 24986)
-- Name: form; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE form (
    id integer NOT NULL,
    name text,
    description text,
    layout text,
    "table" text NOT NULL,
    "tableFields" text
);


ALTER TABLE form OWNER TO postgres;

--
-- TOC entry 189 (class 1259 OID 25650)
-- Name: form_i18n_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE form_i18n_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE form_i18n_id_seq OWNER TO postgres;

--
-- TOC entry 2153 (class 0 OID 0)
-- Dependencies: 189
-- Name: form_i18n_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE form_i18n_id_seq OWNED BY field_i18n.id;


--
-- TOC entry 185 (class 1259 OID 24984)
-- Name: form_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE form_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE form_id_seq OWNER TO postgres;

--
-- TOC entry 2154 (class 0 OID 0)
-- Dependencies: 185
-- Name: form_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE form_id_seq OWNED BY form.id;


--
-- TOC entry 2017 (class 2604 OID 25000)
-- Name: field id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY field ALTER COLUMN id SET DEFAULT nextval('field_id_seq'::regclass);


--
-- TOC entry 2018 (class 2604 OID 25655)
-- Name: field_i18n id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY field_i18n ALTER COLUMN id SET DEFAULT nextval('form_i18n_id_seq'::regclass);


--
-- TOC entry 2016 (class 2604 OID 24989)
-- Name: form id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY form ALTER COLUMN id SET DEFAULT nextval('form_id_seq'::regclass);


--
-- TOC entry 2020 (class 2606 OID 24994)
-- Name: form form_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY form
    ADD CONSTRAINT form_pkey PRIMARY KEY (id);


--
-- TOC entry 2022 (class 2606 OID 25005)
-- Name: field pkey_field; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY field
    ADD CONSTRAINT pkey_field PRIMARY KEY (id);


--
-- TOC entry 2024 (class 2606 OID 25660)
-- Name: field_i18n pkey_field_i18n; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY field_i18n
    ADD CONSTRAINT pkey_field_i18n PRIMARY KEY (id);


--
-- TOC entry 2026 (class 2606 OID 25661)
-- Name: field_i18n fkey_field; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY field_i18n
    ADD CONSTRAINT fkey_field FOREIGN KEY (field_id) REFERENCES field(id);


--
-- TOC entry 2025 (class 2606 OID 25006)
-- Name: field fkey_form; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY field
    ADD CONSTRAINT fkey_form FOREIGN KEY (form_id) REFERENCES form(id);


-- Completed on 2017-06-08 11:18:19

--
-- PostgreSQL database dump complete
--

