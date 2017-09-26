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


