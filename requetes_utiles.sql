-- Requete d'extraction des parts de marchés dans l'entrant
select
substr(pr.ID,17,2) as ENERGIE,cs.SYSTEME_CHAUD, pr.ANNEE,sum(pr.SURFACES)/t.surf_tot as PM
from PARC_RESULTATS pr
join CODE_SYS cs on cs.CODE=substr(pr.ID,13,2)
join (select ANNEE,sum(SURFACES) as surf_tot from PARC_RESULTATS 
where substr(ID,11,2)='05'
group by ANNEE)t on t.ANNEE=pr.ANNEE
where substr(pr.ID,11,2)='05'
group by substr(pr.ID,17,2),cs.SYSTEME_CHAUD, pr.ANNEE, t.surf_tot

-- Requete extraction des couts intangibles dans les systèmes existants
select
ci.BRANCHE, ci.BAT_TYPE, ci.PRODUCTION_CHAUD,ci.ENERGIE,ci.BESOIN_U,
ci.COUT,ci.COUT_ENERGIE,ci.DUREE_VIE,ci.PM_2009,ci.RDT,
cs.CINT,cs.COUT_VARIABLE
from CINT_SYS_EXISTANT cs
join CALIBRATION_CI ci on ci.BRANCHE=cs.BRANCHE and ci.ENERGIE=cs.ENERGIE
and ci.PRODUCTION_CHAUD=cs.PRODUCTION_CHAUD and ci.BAT_TYPE=cs.BAT_TYPE

-- Requete extraction des couts intangibles dans les systèmes neufs
select
ci.BRANCHE, ci.BAT_TYPE, ci.PRODUCTION_CHAUD,ci.ENERGIE,
ci.COUT,ci.DUREE_VIE,ci.PM_ENTRANT,ci.RDT,
cs.CINT,cs.COUT_VARIABLE
from CINT_SYS_NEUF cs
join CALIBRATION_CI_NEUF ci on ci.BRANCHE=cs.BRANCHE and ci.ENERGIE=cs.ENERGIE
and ci.PRODUCTION_CHAUD=cs.PRODUCTION_CHAUD and ci.BAT_TYPE=cs.BAT_TYPE

-- Requete extraction des couts intangibles des gestes batis
select
ci.BRANCHE, ci.GESTE, ci.CHARGE_INIT, ci.COUT_MOY, ci.DUREE_VIE, ci.GAIN_MOY, ci.PART_MARCHE,
cs.CINT,cs.COUT_VARIABLE
from CINT_BATI cs
join CALIBRATION_CI_BATI ci on ci.BRANCHE=cs.BRANCHE and ci.GESTE=cs.GESTE

-- Requete d'extraction des besoins pour le chauffage par segment

select cs.ID , cs.ANNEE, cs.SURFACES,
ci.ID, ci.USAGE, ci.ANNEE, ci.BESOIN, ci.BESOIN/cs.SURFACES as BESOIN_U 
 FROM PARC_RESULTATS cs JOIN BESOIN_RT_RESULTATS ci on ci.ID = cs.ID 
 WHERE ci.USAGE = 'Chauffage'
  
 -- Requete d'extraction des besoins pour le chauffage par énergie et par branche
  
select t.ANNEE, t.COD_ENERGIE, sum(t.SURFACES) as SURFACES, sum(t.BESOIN) as BESOIN,  sum(t.BESOIN)/sum(t.SURFACES)  as BESOIN_U FROM 
(select cs.ID , cs.ANNEE, cs.SURFACES, substr(cs.ID,17,2) as COD_ENERGIE,
ci.USAGE, ci.BESOIN, ci.BESOIN/cs.SURFACES as BESOIN_U 
 FROM PARC_RESULTATS cs JOIN BESOIN_RT_RESULTATS ci on ci.ID = cs.ID  and ci.ANNEE = cs.ANNEE
 WHERE ci.USAGE = 'Chauffage') t
 group by t.ANNEE, t.COD_ENERGIE
 
  -- Identifiants de l'énergie selon les usages

SELECT DISTINCT t.COD_ENERGIE FROM 
(select ci.USAGE, ci.ID, substr(ci.ID,17,2) as COD_ENERGIE  FROM BESOIN_RT_RESULTATS ci   WHERE ci.USAGE = 'Chauffage') t
SELECT DISTINCT t.COD_ENERGIE FROM
 (select ci.USAGE, ci.ID, substr(ci.ID,15,2) as COD_ENERGIE FROM BESOIN_RT_RESULTATS ci   WHERE ci.USAGE = 'Climatisation') t

SELECT DISTINCT t.COD_ENERGIE FROM 
(select ci.USAGE, ci.ID, substr(ci.ID,14,2) as COD_ENERGIE FROM BESOIN_RT_RESULTATS ci   WHERE ci.USAGE = 'ECS') t

--- Comparaisons besoins et conso tot 
SELECT sum(ci.CONSOMMATION_EF) as CONSO_TOT, ci.ANNEE, ci.USAGE FROM CONSO_RT_RESULTATS ci 
WHERE (ci.USAGE != 'Chauffage' AND ci.USAGE != 'Climatisation' AND  ci.USAGE != 'ECS')
GROUP BY ci.USAGE, ci.ANNEE

SELECT sum(cs.BESOIN) as BESOIN_TOT, cs.ANNEE, cs.USAGE FROM BESOIN_RT_RESULTATS cs
WHERE cs.USAGE != 'Chauffage' AND cs.USAGE != 'Climatisation' AND  cs.USAGE != 'ECS' 
GROUP BY cs.USAGE, cs.ANNEE

SELECT a.ANNEE,a.USAGE,a.CONSO_TOT,B.BESOIN_TOT FROM 
(SELECT sum(ci.CONSOMMATION_EF) as CONSO_TOT, ci.ANNEE, ci.USAGE FROM CONSO_RT_RESULTATS ci 
GROUP BY ci.USAGE, ci.ANNEE) a 
JOIN (
SELECT sum(cs.BESOIN) as BESOIN_TOT, cs.ANNEE, cs.USAGE FROM BESOIN_RT_RESULTATS cs
GROUP BY cs.USAGE, cs.ANNEE) b
on a.ANNEE = b.ANNEE AND a.USAGE = b.USAGE

--- Comparaisons besoins et conso tot par énergie 
SELECT r.ANNEE,r.USAGE,r.COD_ENERGIE, r.CONSO_TOT,s.BESOIN_TOT FROM 
(SELECT sum(a.CONSOMMATION_EF) as CONSO_TOT, a.ANNEE, a.USAGE, a.COD_ENERGIE FROM
(SELECT ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF,
(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE 
FROM CONSO_RT_RESULTATS ci
) a  
GROUP BY a.USAGE, a.ANNEE, a.COD_ENERGIE) r
JOIN 
(SELECT sum(b.BESOIN) as BESOIN_TOT, b.ANNEE, b.USAGE, b.COD_ENERGIE FROM
(SELECT cs.ANNEE, cs.USAGE, cs.BESOIN,
(case when cs.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(cs.ID,length(cs.ID)-1,2) end) as COD_ENERGIE 
FROM BESOIN_RT_RESULTATS cs
) b
GROUP BY b.USAGE, b.ANNEE, b.COD_ENERGIE) s
on r.ANNEE = s.ANNEE AND r.USAGE = s.USAGE AND r.COD_ENERGIE = s.COD_ENERGIE


-- Requete d'extraction des besoins et conso pour tous les usages par branche et par énergie
SELECT r.COD_BRANCHE, r.ANNEE,r.USAGE,r.COD_ENERGIE, r.CONSO_TOT,s.BESOIN_TOT FROM 
(SELECT a.COD_BRANCHE, a.ANNEE, a.USAGE, a.COD_ENERGIE,  sum(a.CONSOMMATION_EF) as CONSO_TOT FROM
(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF,
(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE 
FROM CONSO_RT_RESULTATS ci
) a  
GROUP BY a.COD_BRANCHE, a.ANNEE, a.USAGE,  a.COD_ENERGIE) r
JOIN 
(SELECT  b.COD_BRANCHE, b.ANNEE, b.USAGE, b.COD_ENERGIE,sum(b.BESOIN) as BESOIN_TOT FROM
(SELECT substr(cs.ID,1,2) as COD_BRANCHE,cs.ANNEE, cs.USAGE, cs.BESOIN,
(case when cs.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(cs.ID,length(cs.ID)-1,2) end) as COD_ENERGIE 
FROM BESOIN_RT_RESULTATS cs
) b
GROUP BY b.COD_BRANCHE,b.ANNEE, b.USAGE,  b.COD_ENERGIE) s
on r.COD_BRANCHE= s.COD_BRANCHE AND r.ANNEE = s.ANNEE AND r.USAGE = s.USAGE AND r.COD_ENERGIE = s.COD_ENERGIE


-- Requete d'extraction des conso pour tous les usages non rt par branche et par énergie
SELECT r.COD_BRANCHE, r.ANNEE,r.USAGE,r.COD_ENERGIE, r.CONSO_TOT FROM 
(SELECT a.COD_BRANCHE, a.ANNEE, a.USAGE, a.COD_ENERGIE,  sum(a.CONSOMMATION_EF) as CONSO_TOT FROM
(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF,
(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE 
FROM CONSO_NON_RT_RESULTATS ci
) a  
GROUP BY a.COD_BRANCHE, a.ANNEE, a.USAGE,  a.COD_ENERGIE) r



-- Requete d'extraction des besoins et conso pour tous les usages par branche et par énergie et par période de construction
SELECT r.COD_BRANCHE, r.ANNEE,r.COD_PERIODE_SIMPLE,r.USAGE,r.COD_ENERGIE, r.CONSO_TOT,s.BESOIN_TOT FROM 
(SELECT a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE, a.COD_ENERGIE,  sum(a.CONSOMMATION_EF) as CONSO_TOT FROM
(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF,
substr(ci.ID,11,2) as COD_PERIODE_SIMPLE,
(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE
FROM CONSO_RT_RESULTATS ci
) a  
GROUP BY a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE,  a.COD_ENERGIE) r
JOIN 
(SELECT  b.COD_BRANCHE, b.ANNEE,b.COD_PERIODE_SIMPLE, b.USAGE, b.COD_ENERGIE,sum(b.BESOIN) as BESOIN_TOT FROM
(SELECT substr(cs.ID,1,2) as COD_BRANCHE,cs.ANNEE, cs.USAGE, cs.BESOIN,substr(cs.ID,11,2) as COD_PERIODE_SIMPLE,
(case when cs.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(cs.ID,length(cs.ID)-1,2) end) as COD_ENERGIE
FROM BESOIN_RT_RESULTATS cs
) b
GROUP BY b.COD_BRANCHE,b.ANNEE,b.COD_PERIODE_SIMPLE, b.USAGE,  b.COD_ENERGIE) s
on r.COD_BRANCHE= s.COD_BRANCHE AND r.ANNEE = s.ANNEE AND r.USAGE = s.USAGE AND r.COD_ENERGIE = s.COD_ENERGIE AND r.COD_PERIODE_SIMPLE = s.COD_PERIODE_SIMPLE


-- Requete d'extraction des conso pour tous les usages non rt par branche et par énergie et par période de construction
SELECT r.COD_BRANCHE, r.ANNEE, r.COD_PERIODE_SIMPLE, r.USAGE,r.COD_ENERGIE, r.CONSO_TOT FROM 
(SELECT a.COD_BRANCHE, a.ANNEE, a.COD_PERIODE_SIMPLE, a.USAGE, a.COD_ENERGIE,  sum(a.CONSOMMATION_EF) as CONSO_TOT FROM
(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF,
substr(ci.ID,11,2) as COD_PERIODE_SIMPLE,
(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE 
FROM CONSO_NON_RT_RESULTATS ci
) a  
GROUP BY a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE,  a.COD_ENERGIE) r

-- Requete d'extraction des surfaces climatisées par branche
SELECT r.COD_BRANCHE, r.ANNEE, r.COD_PERIODE_SIMPLE,r.COD_SYSTEME_FROID, r.SURFACES_TOT FROM 
(SELECT a.COD_BRANCHE, a.ANNEE, a.COD_PERIODE_SIMPLE,a.COD_SYSTEME_FROID, sum(a.SURFACES)  as SURFACES_TOT FROM
(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE,substr(ci.ID,15,2) as COD_SYSTEME_FROID, ci.SURFACES, 
substr(ci.ID,11,2) as COD_PERIODE_SIMPLE
FROM PARC_RESULTATS ci
) a  
GROUP BY a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.COD_SYSTEME_FROID) r


