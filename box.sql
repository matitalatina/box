--
-- PostgreSQL database dump
--

-- Dumped from database version 10.1
-- Dumped by pg_dump version 10.1

-- Started on 2017-12-18 07:23:00 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 6 (class 2615 OID 26554)
-- Name: box; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA box;


ALTER SCHEMA box OWNER TO postgres;

SET search_path = box, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 277 (class 1259 OID 26736)
-- Name: conf; Type: TABLE; Schema: box; Owner: postgres
--

CREATE TABLE conf (
    id integer NOT NULL,
    key text NOT NULL,
    value text
);


ALTER TABLE conf OWNER TO postgres;

--
-- TOC entry 278 (class 1259 OID 26742)
-- Name: conf_id_seq; Type: SEQUENCE; Schema: box; Owner: postgres
--

CREATE SEQUENCE conf_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE conf_id_seq OWNER TO postgres;

--
-- TOC entry 2516 (class 0 OID 0)
-- Dependencies: 278
-- Name: conf_id_seq; Type: SEQUENCE OWNED BY; Schema: box; Owner: postgres
--

ALTER SEQUENCE conf_id_seq OWNED BY conf.id;


--
-- TOC entry 279 (class 1259 OID 26744)
-- Name: field; Type: TABLE; Schema: box; Owner: postgres
--

CREATE TABLE field (
    field_id integer NOT NULL,
    form_id integer NOT NULL,
    type text NOT NULL,
    name text NOT NULL,
    widget text,
    "lookupEntity" text,
    "lookupValueField" text,
    child_form_id integer,
    "masterFields" text,
    "childFields" text,
    "childFilter" text,
    "default" text,
    min integer,
    max integer
);


ALTER TABLE field OWNER TO postgres;

--
-- TOC entry 2517 (class 0 OID 0)
-- Dependencies: 279
-- Name: COLUMN field."childFields"; Type: COMMENT; Schema: box; Owner: postgres
--

COMMENT ON COLUMN field."childFields" IS 'comma separed subform fields to bind with localFields';


--
-- TOC entry 280 (class 1259 OID 26750)
-- Name: field_file; Type: TABLE; Schema: box; Owner: postgres
--

CREATE TABLE field_file (
    field_id integer NOT NULL,
    file_field text NOT NULL,
    thumbnail_field text,
    name_field text NOT NULL
);


ALTER TABLE field_file OWNER TO postgres;

--
-- TOC entry 281 (class 1259 OID 26756)
-- Name: field_i18n; Type: TABLE; Schema: box; Owner: postgres
--

CREATE TABLE field_i18n (
    id integer NOT NULL,
    field_id integer,
    lang character(2),
    label text,
    placeholder text,
    tooltip text,
    hint text,
    "lookupTextField" text
);


ALTER TABLE field_i18n OWNER TO postgres;

--
-- TOC entry 282 (class 1259 OID 26762)
-- Name: field_id_seq; Type: SEQUENCE; Schema: box; Owner: postgres
--

CREATE SEQUENCE field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE field_id_seq OWNER TO postgres;

--
-- TOC entry 2518 (class 0 OID 0)
-- Dependencies: 282
-- Name: field_id_seq; Type: SEQUENCE OWNED BY; Schema: box; Owner: postgres
--

ALTER SEQUENCE field_id_seq OWNED BY field.field_id;


--
-- TOC entry 283 (class 1259 OID 26764)
-- Name: form; Type: TABLE; Schema: box; Owner: postgres
--

CREATE TABLE form (
    form_id integer NOT NULL,
    name text,
    description text,
    layout text,
    entity text NOT NULL,
    "tabularFields" text,
    query text
);


ALTER TABLE form OWNER TO postgres;

--
-- TOC entry 284 (class 1259 OID 26770)
-- Name: form_i18n_id_seq; Type: SEQUENCE; Schema: box; Owner: postgres
--

CREATE SEQUENCE form_i18n_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE form_i18n_id_seq OWNER TO postgres;

--
-- TOC entry 2519 (class 0 OID 0)
-- Dependencies: 284
-- Name: form_i18n_id_seq; Type: SEQUENCE OWNED BY; Schema: box; Owner: postgres
--

ALTER SEQUENCE form_i18n_id_seq OWNED BY field_i18n.id;


--
-- TOC entry 285 (class 1259 OID 26772)
-- Name: form_id_seq; Type: SEQUENCE; Schema: box; Owner: postgres
--

CREATE SEQUENCE form_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE form_id_seq OWNER TO postgres;

--
-- TOC entry 2520 (class 0 OID 0)
-- Dependencies: 285
-- Name: form_id_seq; Type: SEQUENCE OWNED BY; Schema: box; Owner: postgres
--

ALTER SEQUENCE form_id_seq OWNED BY form.form_id;


--
-- TOC entry 286 (class 1259 OID 26774)
-- Name: labels; Type: TABLE; Schema: box; Owner: postgres
--

CREATE TABLE labels (
    id integer NOT NULL,
    lang text NOT NULL,
    key text NOT NULL,
    label text
);


ALTER TABLE labels OWNER TO postgres;

--
-- TOC entry 287 (class 1259 OID 26780)
-- Name: labels_id_seq; Type: SEQUENCE; Schema: box; Owner: postgres
--

CREATE SEQUENCE labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE labels_id_seq OWNER TO postgres;

--
-- TOC entry 2521 (class 0 OID 0)
-- Dependencies: 287
-- Name: labels_id_seq; Type: SEQUENCE OWNED BY; Schema: box; Owner: postgres
--

ALTER SEQUENCE labels_id_seq OWNED BY labels.id;


--
-- TOC entry 2334 (class 2604 OID 26782)
-- Name: conf id; Type: DEFAULT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY conf ALTER COLUMN id SET DEFAULT nextval('conf_id_seq'::regclass);


--
-- TOC entry 2335 (class 2604 OID 29317)
-- Name: field field_id; Type: DEFAULT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field ALTER COLUMN field_id SET DEFAULT nextval('field_id_seq'::regclass);


--
-- TOC entry 2336 (class 2604 OID 26784)
-- Name: field_i18n id; Type: DEFAULT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field_i18n ALTER COLUMN id SET DEFAULT nextval('form_i18n_id_seq'::regclass);


--
-- TOC entry 2337 (class 2604 OID 29309)
-- Name: form form_id; Type: DEFAULT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY form ALTER COLUMN form_id SET DEFAULT nextval('form_id_seq'::regclass);


--
-- TOC entry 2338 (class 2604 OID 26786)
-- Name: labels id; Type: DEFAULT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY labels ALTER COLUMN id SET DEFAULT nextval('labels_id_seq'::regclass);


--
-- TOC entry 2501 (class 0 OID 26736)
-- Dependencies: 277
-- Data for Name: conf; Type: TABLE DATA; Schema: box; Owner: postgres
--

COPY conf (id, key, value) FROM stdin;
1	manual_edit.key_fields	false
\.


--
-- TOC entry 2503 (class 0 OID 26744)
-- Dependencies: 279
-- Data for Name: field; Type: TABLE DATA; Schema: box; Owner: postgres
--

COPY field (field_id, form_id, type, name, widget, "lookupEntity", "lookupValueField", child_form_id, "masterFields", "childFields", "childFilter", "default", min, max) FROM stdin;
2	1	string	locality	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
3	1	integer	fire_id	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
89	6	number	remark_type_id	hidden	\N	\N	\N	\N	\N	\N	3	\N	\N
5	1	subform	remarks	\N	\N	\N	2	fire_id	fire_id	\N	\N	\N	\N
11	3	number	start_date_reliability_id	fullWidth	val_date_reliability	date_reliability_id	\N	\N	\N	\N	\N	\N	\N
8	3	number	fire_id	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
23	3	number	site_id	\N	val_site	site_id	\N	\N	\N	\N	\N	\N	\N
7	2	number	remark_type_id	hidden	\N	\N	\N	\N	\N	\N	1	\N	\N
19	3	number	x_coord	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
20	3	number	y_coord	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
22	3	number	altitude	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
27	3	number	slope	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
46	3	number	a_forest	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
47	3	number	a_grassland	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
48	3	number	a_nonproductive	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
49	3	number	a_total	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
28	3	number	x_bush	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
30	3	number	x_coppice	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
32	3	number	x_coppice_mixed	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
34	3	number	x_selva	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
36	3	number	x_high_forest_hardwood	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
38	3	number	x_high_forest_softwood	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
40	3	number	x_high_forest_mixed	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
42	3	number	x_pioneer	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
44	3	number	x_forestation	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
50	3	number	d_castanea	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
51	3	number	d_quercus	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
52	3	number	d_fagus	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
54	3	number	d_betula	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
55	3	number	d_other_hardwood	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
56	3	number	d_pinus	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
57	3	number	d_picea	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
58	3	number	d_larix	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
59	3	number	d_other_softwood	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
60	3	number	diameter	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
64	3	number	fire_surface	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
65	3	number	fire_crown	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
66	3	number	fire_single_tree	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
67	3	number	fire_subsurface	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
72	3	number	cause_reliability_id	\N	val_cause_reliability	cause_reliability_id	\N	\N	\N	\N	\N	\N	\N
70	3	number	height_damage	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
73	3	string	other_cause	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
74	3	number	f_protection	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
76	3	number	f_economic	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
77	3	number	f_recreation	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
78	3	number	checked	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
79	3	number	data_from_forestdep	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
80	3	number	data_from_firemandep	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
81	3	number	data_from_history	checkbox	\N	\N	\N	\N	\N	\N	\N	\N	\N
82	3	date	alarm_forestdep	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
97	2	number	line_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
13	3	number	end_date_reliability_id	fullWidth	val_date_reliability	date_reliability_id	\N	\N	\N	\N	\N	\N	\N
21	3	number	coord_reliability_id	\N	val_coord_reliability	coord_reliability_id	\N	\N	\N	\N	\N	\N	\N
61	3	number	litter_id	\N	val_layer_abundance	abundance_id	\N	\N	\N	\N	\N	\N	\N
9	3	string	start_date	datetimePicker	\N	\N	\N	\N	\N	\N	\N	\N	\N
12	3	string	end_date	datetimePicker	\N	\N	\N	\N	\N	\N	\N	\N	\N
25	3	number	expo_id	\N	val_exposition	expo_id	\N	\N	\N	\N	\N	\N	\N
63	3	number	bush_layer_id	\N	val_layer_abundance	abundance_id	\N	\N	\N	\N	\N	\N	\N
69	3	number	damage_soil_id	\N	val_damage	damage_id	\N	\N	\N	\N	\N	\N	\N
62	3	number	herb_layer_id	\N	val_layer_abundance	abundance_id	\N	\N	\N	\N	\N	\N	\N
68	3	number	damage_forest_id	\N	val_damage	damage_id	\N	\N	\N	\N	\N	\N	\N
71	3	number	cause_id	\N	val_cause	cause_id	\N	\N	\N	\N	\N	\N	\N
96	3	number	definition_id	\N	val_definition	definition_id	\N	\N	\N	\N	\N	\N	\N
29	3	number	s_bush	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
18	3	string	locality	twoLines	\N	\N	\N	\N	\N	\N	\N	\N	\N
93	8	number	municipality_type_id	hidden	\N	\N	\N	\N	\N	\N	1	\N	\N
102	6	number	fire_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
31	3	number	s_coppice	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
33	3	number	s_coppice_mixed	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
35	3	number	s_selva	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
39	3	number	s_high_forest_softwood	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
41	3	number	s_high_forest_mixed	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
43	3	number	s_pioneer	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
45	3	number	s_forestation	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
95	10	number	municipality_type_id	hidden	\N	\N	\N	\N	\N	\N	2	\N	\N
92	8	number	municipality_id	popup	municipality	municipality_id	\N	\N	\N	\N	\N	\N	\N
94	10	number	municipality_id	popup	municipality	municipality_id	\N	\N	\N	\N	\N	\N	\N
98	2	number	fire_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
99	5	number	line_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
100	5	number	fire_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
101	6	number	line_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
103	7	number	line_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
87	5	number	remark_type_id	hidden	\N	\N	\N	\N	\N	\N	2	\N	\N
86	5	string	remark	textarea	\N	\N	\N	\N	\N	\N	\N	\N	\N
37	3	number	s_high_forest_hardwood	nolabel	\N	\N	\N	\N	\N	\N	\N	\N	\N
104	7	number	fire_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
105	8	number	fire_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
106	10	number	fire_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
91	7	number	remark_type_id	hidden	\N	\N	\N	\N	\N	\N	4	\N	\N
83	3	child	remark_data_from	\N	\N	\N	6	fire_id	fire_id	[\r\n    {\r\n      "column" : "remark_type_id",\r\n      "operator" : "=",\r\n      "value" : "3"\r\n    }\r\n  ]	\N	\N	\N
84	3	child	remark_notes	\N	\N	\N	7	fire_id	fire_id	[\r\n    {\r\n      "column" : "remark_type_id",\r\n      "operator" : "=",\r\n      "value" : "4"\r\n    }\r\n  ]	\N	\N	\N
85	3	child	remark_cause	\N	\N	\N	5	fire_id	fire_id	[\r\n    {\r\n      "column" : "remark_type_id",\r\n      "operator" : "=",\r\n      "value" : "2"\r\n    }\r\n  ]	\N	\N	\N
24	3	child	remarks_starting_point	\N	\N	\N	2	fire_id	fire_id	[\r\n    {\r\n      "column" : "remark_type_id",\r\n      "operator" : "=",\r\n      "value" : "1"\r\n    }\r\n  ]\r\n	\N	\N	\N
88	6	string	remark	textarea	\N	\N	\N	\N	\N	\N	\N	\N	\N
90	7	string	remark	textarea	\N	\N	\N	\N	\N	\N	\N	\N	\N
6	2	string	remark	textarea	\N	\N	\N	\N	\N	\N	\N	\N	\N
107	2	number	order	hidden	\N	\N	\N	\N	\N	\N	arrayIndex	\N	\N
108	5	number	order	hidden	\N	\N	\N	\N	\N	\N	arrayIndex	\N	\N
109	6	number	order	hidden	\N	\N	\N	\N	\N	\N	arrayIndex	\N	\N
110	7	number	order	hidden	\N	\N	\N	\N	\N	\N	arrayIndex	\N	\N
113	11	file	name	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
114	11	number	document_type_id	\N	document_type	document_type_id	\N	\N	\N	\N	\N	\N	\N
116	11	number	document_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
115	11	number	fire_id	hidden	\N	\N	\N	\N	\N	\N	auto	\N	\N
15	3	child	fire_municipality_start	\N	\N	\N	8	fire_id	fire_id	[\r\n    {\r\n      "column" : "municipality_type_id",\r\n      "operator" : "=",\r\n      "value" : "1"\r\n    }\r\n  ]\r	\N	1	1
17	3	child	fire_municipality_touched	\N	\N	\N	10	fire_id	fire_id	[\r\n    {\r\n      "column" : "municipality_type_id",\r\n      "operator" : "=",\r\n      "value" : "2"\r\n    }\r\n  ]\r	\N	\N	\N
111	3	child	fire_document	\N	\N	\N	11	fire_id	fire_id	\N	\N	\N	\N
\.


--
-- TOC entry 2504 (class 0 OID 26750)
-- Dependencies: 280
-- Data for Name: field_file; Type: TABLE DATA; Schema: box; Owner: postgres
--

COPY field_file (field_id, file_field, thumbnail_field, name_field) FROM stdin;
113	b_document	b_thumbnail	name
\.


--
-- TOC entry 2505 (class 0 OID 26756)
-- Dependencies: 281
-- Data for Name: field_i18n; Type: TABLE DATA; Schema: box; Owner: postgres
--

COPY field_i18n (id, field_id, lang, label, placeholder, tooltip, hint, "lookupTextField") FROM stdin;
2	3	en	Fire ID	201701012	\N	\N	\N
1	2	en	Locality	i.e. Cadenazzo	\N	\N	\N
3	5	en	Remarks	\N	\N	\N	\N
4	6	en	Remark	\N	\N	\N	\N
5	8	en	Fire ID	\N	\N	\N	\N
6	9	en	Start date	\N	\N	\N	\N
8	12	en	Date end of fire	\N	\N	\N	\N
12	18	en	Locality	\N	\N	\N	\N
13	19	en	Coordinates   x	\N	\N	\N	\N
14	20	en	Coordinates y	\N	\N	\N	\N
16	22	en	Altitude	\N	\N	\N	\N
18	24	en	\N	\N	\N	\N	\N
112	19	it	Coordinate x	\N	\N	\N	\N
20	27	en	Slope	\N	\N	\N	\N
21	28	en	Bush	\N	\N	\N	\N
22	29	en	\N	\N	\N	\N	\N
23	30	en	Coppice	\N	\N	\N	\N
24	31	en	\N	\N	\N	\N	\N
25	32	en	Coppice mixed	\N	\N	\N	\N
113	20	it	Coordinate y	\N	\N	\N	\N
26	33	en	\N	\N	\N	\N	\N
27	34	en	Selva	\N	\N	\N	\N
28	35	en	\N	\N	\N	\N	\N
29	36	en	High forest hardwood	\N	\N	\N	\N
30	37	en	\N	\N	\N	\N	\N
31	38	en	High forest softwood	\N	\N	\N	\N
32	39	en	\N	\N	\N	\N	\N
33	40	en	High forest mixed	\N	\N	\N	\N
34	41	en	\N	\N	\N	\N	\N
35	42	en	Pioneer	\N	\N	\N	\N
36	43	en	\N	\N	\N	\N	\N
37	44	en	Forestation	\N	\N	\N	\N
38	45	en	\N	\N	\N	\N	\N
39	46	en	Forest	\N	\N	\N	\N
40	47	en	Grassland	\N	\N	\N	\N
41	48	en	Nonproductive	\N	\N	\N	\N
42	49	en	Total	\N	\N	\N	\N
43	50	en	Chestnut	\N	\N	\N	\N
44	51	en	Oak	\N	\N	\N	\N
45	52	en	Beech	\N	\N	\N	\N
46	54	en	Birch	\N	\N	\N	\N
47	55	en	Other hardwood	\N	\N	\N	\N
48	56	en	Pine	\N	\N	\N	\N
115	22	it	Altitudine	\N	\N	\N	\N
116	23	it	Zona	\N	\N	\N	\N
114	21	it	Precisione	\N	\N	\N	distance
49	57	en	Fir	\N	\N	\N	\N
50	58	en	Larch	\N	\N	\N	\N
51	59	en	Other softwood	\N	\N	\N	\N
52	60	en	Average diameter	\N	\N	\N	\N
56	64	en	Surface	\N	\N	\N	\N
57	65	en	Crown	\N	\N	\N	\N
58	66	en	Single tree	\N	\N	\N	\N
59	67	en	Subsarface	\N	\N	\N	\N
62	70	en	Height of damage on trees	\N	\N	\N	\N
65	73	en	Other cause	\N	\N	\N	\N
66	74	en	Protection	\N	\N	\N	\N
67	76	en	Economic	\N	\N	\N	\N
68	77	en	Recreation/social	\N	\N	\N	\N
69	78	en	Checked	\N	\N	\N	\N
70	79	en	Data from forest service	\N	\N	\N	\N
71	80	en	Data from fire service	\N	\N	\N	\N
72	81	en	Data from historic source	\N	\N	\N	\N
73	82	en	Alarm forest service	\N	\N	\N	\N
74	83	en	\N	\N	\N	\N	\N
75	84	en	Notes	\N	\N	\N	\N
76	85	en	Remarks	\N	\N	\N	\N
77	86	en	Data source remarks	\N	\N	\N	\N
78	87	en	\N	\N	\N	\N	\N
79	88	en	Data source remarks	\N	\N	\N	\N
80	89	en	\N	\N	\N	\N	\N
81	90	en	Data source remarks	\N	\N	\N	\N
82	91	en	\N	\N	\N	\N	\N
84	93	en	\N	\N	\N	\N	\N
86	95	en	\N	\N	\N	\N	\N
10	15	en	Municipality start	\N	\N	\N	\N
11	17	en	Municipality affected	\N	\N	\N	\N
7	11	en	Accuracy start date	\N	\N	\N	en
9	13	en	Accuracy end date	\N	\N	\N	en
15	21	en	Precision	\N	\N	\N	distance
17	23	en	Site	\N	\N	\N	en
19	25	en	Exposition	\N	\N	\N	en
53	61	en	Litter	\N	\N	\N	en
54	62	en	Herb layer	\N	\N	\N	en
55	63	en	Bush layer	\N	\N	\N	en
60	68	en	Damage forest	\N	\N	\N	en
61	69	en	Damage soil	\N	\N	\N	en
63	71	en	Cause	\N	\N	\N	en
64	72	en	Reliability	\N	\N	\N	en
87	96	en	Definition	\N	\N	\N	en
88	97	en	\N	\N	\N	\N	\N
89	98	en	\N	\N	\N	\N	\N
90	99	en	\N	\N	\N	\N	\N
91	100	en	\N	\N	\N	\N	\N
93	102	en	\N	\N	\N	\N	\N
95	104	en	\N	\N	\N	\N	\N
96	7	en	\N	\N	\N	\N	\N
83	92	en	Municipality	\N	\N	\N	shortname
85	94	en	Municipality	\N	\N	\N	shortname
97	105	en	\N	\N	\N	\N	\N
98	106	en	\N	\N	\N	\N	\N
92	101	en	\N	\N	\N	\N	longname
94	103	en	\N	\N	\N	\N	longname
99	2	it	Località	\N	\N	\N	\N
100	3	it	ID incendio	\N	\N	\N	\N
101	5	it	\N	\N	\N	\N	\N
102	6	it	Note	\N	\N	\N	\N
103	7	it	\N	\N	\N	\N	\N
104	8	it	ID incendio	\N	\N	\N	\N
105	9	it	Data prima segnalazione	\N	\N	\N	\N
106	11	it	Precisione	\N	\N	\N	\N
107	12	it	Data fine incendio	\N	\N	\N	\N
108	13	it	\N	\N	\N	\N	\N
109	15	it	\N	\N	\N	\N	\N
110	17	it	\N	\N	\N	\N	\N
111	18	it	Località	\N	\N	\N	\N
117	24	it	\N	\N	\N	\N	\N
121	29	it	\N	\N	\N	\N	\N
123	31	it	\N	\N	\N	\N	\N
125	33	it	\N	\N	\N	\N	\N
127	35	it	\N	\N	\N	\N	\N
129	37	it	\N	\N	\N	\N	\N
131	39	it	\N	\N	\N	\N	\N
133	41	it	\N	\N	\N	\N	\N
135	43	it	\N	\N	\N	\N	\N
137	45	it	\N	\N	\N	\N	\N
197	2	fr	Localité	\N	\N	\N	\N
173	83	it	\N	\N	\N	\N	\N
177	87	it	\N	\N	\N	\N	\N
179	89	it	\N	\N	\N	\N	\N
183	93	it	\N	\N	\N	\N	\N
185	95	it	\N	\N	\N	\N	\N
187	97	it	\N	\N	\N	\N	\N
189	99	it	\N	\N	\N	\N	\N
190	100	it	ID incendio	\N	\N	\N	\N
188	98	it	ID incendio	\N	\N	\N	\N
118	25	it	Esposizione	\N	\N	\N	\N
119	27	it	Pendenza	\N	\N	\N	\N
120	28	it	Arbusteto	\N	\N	\N	\N
122	30	it	Ceduo semplice	\N	\N	\N	\N
191	101	it	\N	\N	\N	\N	\N
124	32	it	Ceduo invecchiato/composto	\N	\N	\N	\N
126	34	it	Selva	\N	\N	\N	\N
128	36	it	Alto fusto latifoglie	\N	\N	\N	\N
130	38	it	Alto fusto conifere	\N	\N	\N	\N
132	40	it	Alto fusto misto	\N	\N	\N	\N
198	3	fr	ID incendie	\N	\N	\N	\N
134	42	it	Bosco pioniere	\N	\N	\N	\N
136	44	it	Piantagioni	\N	\N	\N	\N
138	46	it	Bosco	\N	\N	\N	\N
139	47	it	Pascolo e prato	\N	\N	\N	\N
140	48	it	Improduttivo	\N	\N	\N	\N
141	49	it	Totale	\N	\N	\N	\N
142	50	it	Castagno	\N	\N	\N	\N
143	51	it	Quercia	\N	\N	\N	\N
144	52	it	Faggio	\N	\N	\N	\N
145	54	it	Betulla	\N	\N	\N	\N
146	55	it	Altre latifoglie	\N	\N	\N	\N
147	56	it	Pino	\N	\N	\N	\N
148	57	it	Abete rosso	\N	\N	\N	\N
149	58	it	Larice	\N	\N	\N	\N
150	59	it	Altre conifere	\N	\N	\N	\N
151	60	it	Diametro	\N	\N	\N	\N
152	61	it	Lettiera	\N	\N	\N	\N
153	62	it	Strato erbaceo	\N	\N	\N	\N
154	63	it	Strato arbustivo	\N	\N	\N	\N
155	64	it	di superficie	\N	\N	\N	\N
156	65	it	di chioma	\N	\N	\N	\N
157	66	it	albero singolo	\N	\N	\N	\N
158	67	it	sotterraneo	\N	\N	\N	\N
159	68	it	Danni al bosco	\N	\N	\N	\N
160	69	it	Danni al suolo	\N	\N	\N	\N
161	70	it	Altezza scottature sugli alberi	\N	\N	\N	\N
162	71	it	Causa	\N	\N	\N	\N
163	72	it	Affidabilità	\N	\N	\N	\N
164	73	it	Altra causa conosciuta	\N	\N	\N	\N
165	74	it	Protezione	\N	\N	\N	\N
166	76	it	Economica	\N	\N	\N	\N
167	77	it	Ricreativa/sociale	\N	\N	\N	\N
168	78	it	Controllato	\N	\N	\N	\N
169	79	it	Dati al servizio forestale	\N	\N	\N	\N
170	80	it	Dati del corpo pompieri	\N	\N	\N	\N
171	81	it	Dati da fonte storica	\N	\N	\N	\N
172	82	it	Allarme servizio forestale	\N	\N	\N	\N
200	6	fr	Observations	\N	\N	\N	\N
174	84	it	Osservazioni generali	\N	\N	\N	\N
202	8	fr	ID incendie	\N	\N	\N	\N
175	85	it	Osservazioni	 	\N	\N	\N
176	86	it	Note	\N	\N	\N	\N
178	88	it	Note	\N	\N	\N	\N
180	90	it	Note	\N	\N	\N	\N
203	9	fr	Date première déclaration d'incendie	\N	\N	\N	\N
181	91	it	\N	\N	\N	\N	\N
184	94	it	Comune	\N	\N	\N	\N
186	96	it	Definizione	\N	\N	\N	\N
193	103	it	\N	\N	\N	\N	\N
192	102	it	ID incendio	\N	\N	\N	\N
194	104	it	ID incendio	\N	\N	\N	\N
195	105	it	ID incendio	\N	\N	\N	\N
196	106	it	ID incendio	\N	\N	\N	\N
199	5	fr	\N	\N	\N	\N	\N
201	7	fr	\N	\N	\N	\N	\N
206	13	fr	\N	\N	\N	\N	\N
207	15	fr	\N	\N	\N	\N	\N
208	17	fr	\N	\N	\N	\N	\N
215	24	fr	\N	\N	\N	\N	\N
219	29	fr	\N	\N	\N	\N	\N
204	11	fr	Précision	\N	\N	\N	\N
205	12	fr	Date fin de l'incendie	\N	\N	\N	\N
209	18	fr	Localité	\N	\N	\N	\N
210	19	fr	Coordonnées x	\N	\N	\N	\N
211	20	fr	Coordonnées y	\N	\N	\N	\N
213	22	fr	Altitude	\N	\N	\N	\N
214	23	fr	Lieu	\N	\N	\N	\N
216	25	fr	Exposition	\N	\N	\N	\N
217	27	fr	Pente	\N	\N	\N	\N
218	28	fr	Forêt buissonnante	\N	\N	\N	\N
182	92	it	Comune	\N	\N	\N	shortname
212	21	fr	Précision	\N	\N	\N	distance
221	31	fr	\N	\N	\N	\N	\N
223	33	fr	\N	\N	\N	\N	\N
225	35	fr	\N	\N	\N	\N	\N
227	37	fr	\N	\N	\N	\N	\N
229	39	fr	\N	\N	\N	\N	\N
231	41	fr	\N	\N	\N	\N	\N
233	43	fr	\N	\N	\N	\N	\N
235	45	fr	\N	\N	\N	\N	\N
271	83	fr	\N	\N	\N	\N	\N
275	87	fr	\N	\N	\N	\N	\N
277	89	fr	\N	\N	\N	\N	\N
279	91	fr	\N	\N	\N	\N	\N
281	93	fr	\N	\N	\N	\N	\N
283	95	fr	\N	\N	\N	\N	\N
285	97	fr	\N	\N	\N	\N	\N
287	99	fr	\N	\N	\N	\N	\N
289	101	fr	\N	\N	\N	\N	\N
291	103	fr	\N	\N	\N	\N	\N
220	30	fr	Taillis	\N	\N	\N	\N
222	32	fr	Taillis-sous-futaie	\N	\N	\N	\N
224	34	fr	Selve	\N	\N	\N	\N
226	36	fr	Futaie feuillue	\N	\N	\N	\N
228	38	fr	Futaie rèsineuse	\N	\N	\N	\N
230	40	fr	Futaie mélangée	\N	\N	\N	\N
232	42	fr	Forêt pionnière	  	\N	\N	\N
234	44	fr	Plantations	\N	\N	\N	\N
236	46	fr	Forêt	\N	\N	\N	\N
237	47	fr	Pré et pâturage	\N	\N	\N	\N
238	48	fr	Improductif	\N	\N	\N	\N
239	49	fr	Total	\N	\N	\N	\N
240	50	fr	Châtaigner	\N	\N	\N	\N
241	51	fr	Chêne	\N	\N	\N	\N
242	52	fr	Hêtre	\N	\N	\N	\N
243	54	fr	Bouleau	\N	\N	\N	\N
244	55	fr	Autres feuillis	\N	\N	\N	\N
245	56	fr	Pin	\N	\N	\N	\N
246	57	fr	Epicéa	\N	\N	\N	\N
247	58	fr	Mélèze	\N	\N	\N	\N
248	59	fr	Autres résineux	\N	\N	\N	\N
249	60	fr	Diamètre	\N	\N	\N	\N
250	61	fr	Litière	\N	\N	\N	\N
251	62	fr	Strate herbacé	\N	\N	\N	\N
252	63	fr	Strate arbustiv	\N	\N	\N	\N
253	64	fr	de surface	\N	\N	\N	\N
254	65	fr	de couronne	\N	\N	\N	\N
255	66	fr	arbre seul	\N	\N	\N	\N
256	67	fr	souterrain	\N	\N	\N	\N
258	69	fr	Dègâts au terrain	\N	\N	\N	\N
257	68	fr	Dégâts à la forêt	\N	\N	\N	\N
259	70	fr	Hauteur des brûlures	\N	\N	\N	\N
260	71	fr	Cause	\N	\N	\N	\N
261	72	fr	Fiabilité	\N	\N	\N	\N
262	73	fr	Autre cause connue	\N	\N	\N	\N
263	74	fr	Protection	\N	\N	\N	\N
264	76	fr	Economique	\N	\N	\N	\N
265	77	fr	Récréative/sociale	\N	\N	\N	\N
266	78	fr	Contrôlé	\N	\N	\N	\N
267	79	fr	Données du service forestier	\N	\N	\N	\N
268	80	fr	Données du corp pompiers	\N	\N	\N	\N
269	81	fr	Données depuis source historique	\N	\N	\N	\N
270	82	fr	Alarme service forestier	\N	\N	\N	\N
272	84	fr	Observations générale	\N	\N	\N	\N
273	85	fr	Observations	\N	\N	\N	\N
274	86	fr	Observations	\N	\N	\N	\N
276	88	fr	Observations	\N	\N	\N	\N
278	90	fr	Observations	\N	\N	\N	\N
282	94	fr	Commune	\N	\N	\N	\N
284	96	fr	Définition	\N	\N	\N	\N
286	98	fr	ID incendie	\N	\N	\N	\N
288	100	fr	ID incendie	\N	\N	\N	\N
290	102	fr	ID incendie	\N	\N	\N	\N
292	104	fr	ID incendie	\N	\N	\N	\N
293	105	fr	ID incendie	\N	\N	\N	\N
294	106	fr	ID incendie	\N	\N	\N	\N
296	3	ge	ID Brand	\N	\N	\N	\N
297	5	ge	\N	\N	\N	\N	\N
299	7	ge	\N	\N	\N	\N	\N
304	13	ge	\N	\N	\N	\N	\N
305	15	ge	\N	\N	\N	\N	\N
306	17	ge	\N	\N	\N	\N	\N
313	24	ge	\N	\N	\N	\N	\N
317	29	ge	\N	\N	\N	\N	\N
319	31	ge	\N	\N	\N	\N	\N
321	33	ge	\N	\N	\N	\N	\N
323	35	ge	\N	\N	\N	\N	\N
325	37	ge	\N	\N	\N	\N	\N
327	39	ge	\N	\N	\N	\N	\N
329	41	ge	\N	\N	\N	\N	\N
331	43	ge	\N	\N	\N	\N	\N
333	45	ge	\N	\N	\N	\N	\N
369	83	ge	\N	\N	\N	\N	\N
373	87	ge	\N	\N	\N	\N	\N
375	89	ge	\N	\N	\N	\N	\N
377	91	ge	\N	\N	\N	\N	\N
379	93	ge	\N	\N	\N	\N	\N
381	95	ge	\N	\N	\N	\N	\N
383	97	ge	\N	\N	\N	\N	\N
298	6	ge	Bemerkungen	\N	\N	\N	\N
300	8	ge	ID Brand	\N	\N	\N	\N
302	11	ge	Genaugigkeit	\N	\N	\N	\N
303	12	ge	Datum Brand gelöscht	\N	\N	\N	\N
308	19	ge	Koordinaten x	\N	\N	\N	\N
309	20	ge	Koordinaten y	\N	\N	\N	\N
311	22	ge	Höhe	\N	\N	\N	\N
312	23	ge	Ort	\N	\N	\N	\N
314	25	ge	Exposition	\N	\N	\N	\N
315	27	ge	Neigung	\N	\N	\N	\N
318	30	ge	Niederwald	\N	\N	\N	\N
320	32	ge	Mittelwald	\N	\N	\N	\N
322	34	ge	Kastanienselve	\N	\N	\N	\N
326	38	ge	Hochwald Nadelholz	\N	\N	\N	\N
328	40	ge	Hochwald gemischt	\N	\N	\N	\N
332	44	ge	Aufforstungen	\N	\N	\N	\N
334	46	ge	Wald	\N	\N	\N	\N
336	48	ge	Unproduktiv	\N	\N	\N	\N
337	49	ge	Total	\N	\N	\N	\N
338	50	ge	Kastanie	\N	\N	\N	\N
339	51	ge	Eiche	\N	\N	\N	\N
341	54	ge	Birke	\N	\N	\N	\N
342	55	ge	Andere Laubhölzer	\N	\N	\N	\N
343	56	ge	Föhre	\N	\N	\N	\N
345	58	ge	Lärche	\N	\N	\N	\N
346	59	ge	Andere Nadelhölzer	\N	\N	\N	\N
347	60	ge	Mittlerer	\N	\N	\N	\N
349	62	ge	Krautschicht	\N	\N	\N	\N
350	63	ge	Strauchschicht	\N	\N	\N	\N
352	65	ge	Kronenfeuer	\N	\N	\N	\N
353	66	ge	Stockfeuer	\N	\N	\N	\N
354	67	ge	Unterirdisches Feuer	\N	\N	\N	\N
356	69	ge	Schaden am Boden	\N	\N	\N	\N
358	71	ge	Ursache	\N	\N	\N	\N
359	72	ge	Zuverlässigkeit	\N	\N	\N	\N
361	74	ge	Schutz	\N	\N	\N	\N
362	76	ge	Ökonomisch	\N	\N	\N	\N
363	77	ge	Erholung/sozial	\N	\N	\N	\N
364	78	ge	Kontrolliert	\N	\N	\N	\N
366	80	ge	Daten der Feuerwehr	\N	\N	\N	\N
367	81	ge	Daten aus historische Quelle	\N	\N	\N	\N
370	84	ge	Allgemeine Bemerkungen	\N	\N	\N	\N
371	85	ge	Bemerkungen	\N	\N	\N	\N
374	88	ge	Bemerkungen	\N	\N	\N	\N
376	90	ge	Bemerkungen	\N	\N	\N	\N
380	94	ge	Gemeinde	\N	\N	\N	\N
280	92	fr	Commune	\N	\N	\N	shortname
385	99	ge	\N	\N	\N	\N	\N
387	101	ge	\N	\N	\N	\N	\N
389	103	ge	\N	\N	\N	\N	\N
295	2	ge	Lokalität	\N	\N	\N	\N
301	9	ge	Datum erste Brandmeldung	\N	\N	\N	\N
307	18	ge	Lokalität	\N	\N	\N	\N
316	28	ge	Gebüschwald	\N	\N	\N	\N
324	36	ge	Hochwald Laubholz	\N	\N	\N	\N
330	42	ge	Pionierwald	\N	\N	\N	\N
335	47	ge	Weide und Wiese	\N	\N	\N	\N
340	52	ge	Buche	\N	\N	\N	\N
344	57	ge	Fichte	\N	\N	\N	\N
348	61	ge	Streuschicht	\N	\N	\N	\N
351	64	ge	Bodenfeuer	\N	\N	\N	\N
355	68	ge	Schaden am Waldbestand	\N	\N	\N	\N
357	70	ge	Höhe der Brandspuren	\N	\N	\N	\N
360	73	ge	Andere bekannte Ursache	\N	\N	\N	\N
365	79	ge	Daten des Forstdienstes	\N	\N	\N	\N
368	82	ge	Alarm Forstdienst	\N	\N	\N	\N
372	86	ge	Bemerkungen	\N	\N	\N	\N
382	96	ge	Definition	\N	\N	\N	\N
384	98	ge	ID Brand	\N	\N	\N	\N
386	100	ge	ID Brand	\N	\N	\N	\N
388	102	ge	ID Brand	\N	\N	\N	\N
390	104	ge	ID Brand	\N	\N	\N	\N
391	105	ge	ID Brand	\N	\N	\N	\N
392	106	ge	ID Brand	\N	\N	\N	\N
378	92	ge	Gemeinde	\N	\N	\N	shortname
310	21	ge	Genauigkeit	\N	\N	\N	distance
393	107	en	\N	\N	\N	\N	\N
394	107	it	\N	\N	\N	\N	\N
395	107	fr	\N	\N	\N	\N	\N
396	107	ge	\N	\N	\N	\N	\N
397	108	en	\N	\N	\N	\N	\N
398	108	it	\N	\N	\N	\N	\N
399	108	fr	\N	\N	\N	\N	\N
400	108	ge	\N	\N	\N	\N	\N
401	109	en	\N	\N	\N	\N	\N
402	109	it	\N	\N	\N	\N	\N
403	109	fr	\N	\N	\N	\N	\N
404	109	ge	\N	\N	\N	\N	\N
405	110	it	\N	\N	\N	\N	\N
406	110	en	\N	\N	\N	\N	\N
407	110	fr	\N	\N	\N	\N	\N
408	110	ge	\N	\N	\N	\N	\N
409	111	en	Document	\N	\N	\N	\N
410	111	it	Documenti	\N	\N	\N	\N
411	111	fr	Document	\N	\N	\N	\N
412	111	ge	Document	\N	\N	\N	\N
413	113	ge	Document	\N	\N	\N	\N
414	113	fr	Document	\N	\N	\N	\N
415	113	it	Documenti	\N	\N	\N	\N
416	113	en	Document	\N	\N	\N	\N
420	114	ge	Type	\N	\N	\N	ge
419	114	fr	Type	\N	\N	\N	fr
417	114	it	Tipo	\N	\N	\N	it
418	114	en	Type	\N	\N	\N	en
421	115	en	\N	\N	\N	\N	\N
422	115	it	\N	\N	\N	\N	\N
423	115	ge	\N	\N	\N	\N	\N
424	115	fr	\N	\N	\N	\N	\N
425	116	it	\N	\N	\N	\N	\N
426	116	en	\N	\N	\N	\N	\N
427	116	ge	\N	\N	\N	\N	\N
428	116	fr	\N	\N	\N	\N	\N
\.


--
-- TOC entry 2507 (class 0 OID 26764)
-- Dependencies: 283
-- Data for Name: form; Type: TABLE DATA; Schema: box; Owner: postgres
--

COPY form (form_id, name, description, layout, entity, "tabularFields", query) FROM stdin;
1	test	test	\N	fire	locality	\N
2	remark_starting_point	remarks starting_point	\N	remark	remark	{"sort":[{"column":"order","order":"asc"}], "filter": []}
6	remark_history	remark on history data	\N	remark	remark	\N
5	remark_cause	remarks on cause	\N	remark	remark	\N
7	remark_notes	notes	\N	remark	remark	\N
8	fire_municipality_start	\N	\N	fire_municipality	\N	\N
10	fire_municipalirt_touched	\N	\N	fire_municipality	\N	\N
11	fire_document	documents on fire	\N	document	name	{"sort":[{"column":"document_id","order":"asc"}], "filter": []}
3	fire	fire	{\r\n\t"blocks": [{\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["fire_id", "checked", "definition_id",\r\n\t\t\t\t{\r\n\t\t\t\t\t"fieldsWidth": [6],\r\n\t\t\t\t\t"fields": ["start_date","start_date_reliability_id"]\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t"fieldsWidth": [6],\r\n\t\t\t\t\t"fields": ["end_date","end_date_reliability_id"]\r\n\t\t\t\t}\r\n\t\t\t]\r\n\t\t}, {\r\n\t\t\t"title": "fire.municipality",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["fire_municipality_start", "fire_municipality_touched"]\r\n\t\t}, {\r\n\t\t\t"title": "Innesco",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["locality", "x_coord", "y_coord", "coord_reliability_id", "altitude", "site_id", "remarks_starting_point"]\r\n\t\t},{\r\n          \t\t\t"width": 12,\r\n          \t\t\t"fields": []\r\n          \t\t},{\r\n\t\t\t"title": "Rilievo",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["expo_id", "slope"]\r\n\t\t}, {\r\n\t\t\t"title": "Superficie percorsa",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["a_forest", "a_grassland", "a_nonproductive", "a_total"]\r\n\t\t}, {\r\n\t\t\t"width": 12,\r\n\t\t\t"fields": []\r\n\t\t}, {\r\n\t\t\t"title": "Tipologia boschiva",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": [{\r\n\t\t\t\t\t"fieldsWidth": [6],\r\n\t\t\t\t\t"fields": [\r\n\t\t\t\t\t\t"x_bush",\r\n                        "s_bush"\r\n                    ]},\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_coppice",\r\n                            "s_coppice"\r\n                        ]\r\n                    },\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_coppice_mixed",\r\n                            "s_coppice_mixed"\r\n                        ]\r\n                    },\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_selva",\r\n                            "s_selva"\r\n                        ]\r\n                    },\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_high_forest_hardwood",\r\n                            "s_high_forest_hardwood"\r\n                        ]\r\n                    },\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_high_forest_softwood",\r\n                            "s_high_forest_softwood"\r\n                        ]\r\n                    },\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_high_forest_mixed",\r\n                            "s_high_forest_mixed"\r\n                        ]\r\n                    },\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_pioneer",\r\n                            "s_pioneer"\r\n                        ]\r\n                    },\r\n                    {\r\n                        "fieldsWidth": [6],\r\n                        "fields": [\r\n                            "x_forestation",\r\n                            "s_forestation"\r\n                        ]\r\n                    }\r\n\t\t\t\t\t\t\t\t\t\t\r\n\t\t\t\t]\r\n\t\t\t\t\r\n\t\t\t\r\n\t\t}, {\r\n\t\t\t"title": "Specie dominante",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["d_castanea", "d_quercus", "d_fagus", "d_betula", "d_other_hardwood", "d_pinus", "d_picea", "d_larix", "d_other_softwood", "diameter"]\r\n\t\t}, {\r\n\t\t\t"title": "Combustibile e sottobosco",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["litter_id", "herb_layer_id", "bush_layer_id"]\r\n\t\t}, {\r\n\t\t\t"title": "Tipologia incendio",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["fire_surface", "fire_crown", "fire_single_tree", "fire_subsurface"]\r\n\t\t}, {\r\n\t\t\t"title": "Danni",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["damage_forest_id", "damage_soil_id", "height_damage"]\r\n\t\t}, {\r\n\t\t\t"title": "Causa",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["cause_id", "cause_reliability_id", "other_cause", "remark_cause"]\r\n\t\t}, {\r\n\t\t\t"title": "Funzioni prevalenti",\r\n\t\t\t"width": 4,\r\n\t\t\t"fields": ["f_protection", "f_economic", "f_recreation"]\r\n\t\t}, {\r\n\t\t\t"title": "Osservazioni",\r\n\t\t\t"width": 12,\r\n\t\t\t"fields": ["data_from_forestdep", "data_from_firemandep", "data_from_history", "alarm_forestdep", "remark_data_from", "remark_notes"]\r\n\t\t},{\r\n          \t\t\t"title": "Document",\r\n          \t\t\t"width": 12,\r\n          \t\t\t"fields": ["fire_document"]\r\n          \t\t}\r\n\t]\r\n}	fire	locality,start_date,a_forest,a_total,expo_id	{"sort":[{"column":"fire_id","order":"desc"}], "filter": []}
\.


--
-- TOC entry 2510 (class 0 OID 26774)
-- Dependencies: 286
-- Data for Name: labels; Type: TABLE DATA; Schema: box; Owner: postgres
--

COPY labels (id, lang, key, label) FROM stdin;
1	it	test	test
2	en	test	testEN
5	en	message.confirm	Are you sure?
8	en	subform.add	Add
9	en	subform.remove	Remove
3	en	error.notfound	URL not found!
10	en	login.title	Sign In
11	en	login.failed	Login failed
12	en	login.username	Username
13	en	login.password	Password
14	en	login.button	Login
15	en	navigation.next	Next
16	en	navigation.previous	Previous
17	en	form.save	Save
23	en	table.actions	Actions
24	en	table.edit	Edit
25	en	header.home	Home
26	en	header.forms	Forms
28	en	fire.municipality	Municipality
29	it	error.notfound	URL non trovato!
30	it	message.confirm	Sei sicuro?
31	it	subform.add	Aggiungi
32	it	subform.remove	Remove
33	it	login.title	Sign in
34	it	login.failed	Login fallito
35	it	login.username	Nome utente
36	it	login.password	Password
37	it	login.button	Login
38	it	navigation.next	Prossimo
39	it	navigation.previous	Precendente
40	it	form.save	Salva
46	it	table.actions	Azioni
47	it	table.edit	Modifica
48	it	header.home	Home
49	it	header.forms	Maschere
51	it	fire.municipality	Comune
52	it	form.save_add	Salva e aggiungi
53	en	form.save_add	Save and insert next
21	en	entity.new	New
22	en	entity.table	Table
19	en	entity.title	Tables/Views
20	en	entity.select	select your entity
44	it	entity.new	Nuovo
45	it	entity.table	Tabella
18	en	entity.search	Search entity
27	en	header.entities	Table/Views
41	it	entity.search	Cerca entity
42	it	entity.title	Tabelle/Views
43	it	entity.select	Seleziona entity
50	it	header.entities	Entities
\.


--
-- TOC entry 2522 (class 0 OID 0)
-- Dependencies: 278
-- Name: conf_id_seq; Type: SEQUENCE SET; Schema: box; Owner: postgres
--

SELECT pg_catalog.setval('conf_id_seq', 1, false);


--
-- TOC entry 2523 (class 0 OID 0)
-- Dependencies: 282
-- Name: field_id_seq; Type: SEQUENCE SET; Schema: box; Owner: postgres
--

SELECT pg_catalog.setval('field_id_seq', 116, true);


--
-- TOC entry 2524 (class 0 OID 0)
-- Dependencies: 284
-- Name: form_i18n_id_seq; Type: SEQUENCE SET; Schema: box; Owner: postgres
--

SELECT pg_catalog.setval('form_i18n_id_seq', 428, true);


--
-- TOC entry 2525 (class 0 OID 0)
-- Dependencies: 285
-- Name: form_id_seq; Type: SEQUENCE SET; Schema: box; Owner: postgres
--

SELECT pg_catalog.setval('form_id_seq', 11, true);


--
-- TOC entry 2526 (class 0 OID 0)
-- Dependencies: 287
-- Name: labels_id_seq; Type: SEQUENCE SET; Schema: box; Owner: postgres
--

SELECT pg_catalog.setval('labels_id_seq', 52, true);


--
-- TOC entry 2342 (class 2606 OID 26788)
-- Name: field_file field_file_pkey; Type: CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field_file
    ADD CONSTRAINT field_file_pkey PRIMARY KEY (field_id);


--
-- TOC entry 2346 (class 2606 OID 29311)
-- Name: form form_pkey; Type: CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY form
    ADD CONSTRAINT form_pkey PRIMARY KEY (form_id);


--
-- TOC entry 2340 (class 2606 OID 29319)
-- Name: field pkey_field; Type: CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field
    ADD CONSTRAINT pkey_field PRIMARY KEY (field_id);


--
-- TOC entry 2344 (class 2606 OID 26794)
-- Name: field_i18n pkey_field_i18n; Type: CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field_i18n
    ADD CONSTRAINT pkey_field_i18n PRIMARY KEY (id);


--
-- TOC entry 2348 (class 2606 OID 26796)
-- Name: labels pkey_label; Type: CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY labels
    ADD CONSTRAINT pkey_label PRIMARY KEY (id);


--
-- TOC entry 2350 (class 2606 OID 29320)
-- Name: field_file field_file_field_id_fk; Type: FK CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field_file
    ADD CONSTRAINT field_file_field_id_fk FOREIGN KEY (field_id) REFERENCES field(field_id);


--
-- TOC entry 2351 (class 2606 OID 29325)
-- Name: field_i18n fkey_field; Type: FK CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field_i18n
    ADD CONSTRAINT fkey_field FOREIGN KEY (field_id) REFERENCES field(field_id);


--
-- TOC entry 2349 (class 2606 OID 29312)
-- Name: field fkey_form; Type: FK CONSTRAINT; Schema: box; Owner: postgres
--

ALTER TABLE ONLY field
    ADD CONSTRAINT fkey_form FOREIGN KEY (form_id) REFERENCES form(form_id);


-- Completed on 2017-12-18 07:23:00 CET

--
-- PostgreSQL database dump complete
--

