
require(data.table)
require(ggplot2)
require(Hmisc)
require(reshape2)
require(plyr)
require(ggthemes)
require(texreg)
if (Sys.getenv("JAVA_HOME")!="")
  Sys.setenv(JAVA_HOME="")
options(java.parameters = "-Xmx8192m")
require(XLConnect)
require(RColorBrewer)

colorpalette = "Spectral"
fillpalette = "Spectral"

wbfiles  = "../tertiaire_to_export/resultats_AME_run3/2050_allpol/Result_csv"

wbname = c(  "S1 : AME 2018 run2")

source("read_resultats_csv.R", encoding= "UTF-8")

parc_melted[annee == "2009", list(surface = sum(Surface)), by="branche"]

Conso[annee =="2009", list(ConsoTwhEF = sum(value)), by="branche"]


#### parc initial desgagrégé
besoins_chauffage_init = fread("../tertiaire/parametrage modèle/Besoin_chauffage_init.csv", dec = ",", 
                               colClasses =
                                 list("character"=c("ID_segment","ID_AGREG2", "ID_AGREG", "ID_AGREG_RDT_COUT", 
                                                    "ID","ID_BRANCHE","ID_BAT_TYPE","ID_PRODUCTION_CHAUD","ID_ENERGIE")))

besoins_chauffage_init[, ConsoUChauff := BESOIN/`SURFACES 2009`/`RDT systeme`]
besoins_chauffage_init[,ConsoChauff := BESOIN/`RDT systeme`]
besoins_chauffage_init[,sum(ConsoChauff)/10^9]


besoins_chauffage_init[,COD_BRANCHE:=substring(ID, 1,2)]
besoins_chauffage_init[,COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_chauffage_init[,COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_chauffage_init[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_chauffage_init[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]


besoins_chauffage_init[,ID_AGREG_GESTE := 
                         paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                COD_PERIODE_DETAIL,COD_PERIODE_SIMPLE) ]

##### coûts des gestes 

cout_geste = fread("../docs_completementaires_EN/Bibli_geste_bati_new.csv", dec = ",",
                   colClasses = list("character" = c("ID_AGREG","EXIGENCE","GESTE")))

cout_geste[, COD_BRANCHE:=substring(ID_AGREG, 1,2)]
cout_geste[, COD_SS_BRANCHE:=substring(ID_AGREG, 3,4)]
cout_geste[, COD_BAT_TYPE:=substring(ID_AGREG, 5,6)]
cout_geste[, COD_PERIODE_DETAIL :=substring(ID_AGREG, 7,8)]
cout_geste[, COD_PERIODE_SIMPLE:=substring(ID_AGREG, 9,10)]

cout_geste = merge(cout_geste, COD_BRANCHE, by="COD_BRANCHE") 
cout_geste = merge(cout_geste, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
cout_geste = merge(cout_geste, COD_BAT_TYPE, by="COD_BAT_TYPE") 
cout_geste = merge(cout_geste, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL", all.x = T) 
cout_geste = merge(cout_geste, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 

cout_geste[is.na(PERIODE_DETAIL), BRANCHE]


setkeyv(cout_geste,c("BRANCHE","SS_BRANCHE", "BAT_TYPE"))
ordre = unique(cout_geste$SS_BRANCHE)
cout_geste[, SS_BRANCHE:=factor(SS_BRANCHE, levels = ordre )]
cout_geste[, PERIODE_SIMPLE:=factor(PERIODE_SIMPLE)]
cout_geste[, PERIODE_SIMPLE:=relevel(PERIODE_SIMPLE, ref = "Av 1980")]

unique(cout_geste$PERIODE_SIMPLE)

cout_geste[GAIN == 0, GAIN:=NA]
cout_geste[COUT == 0, COUT:=NA]

cout_geste_casted = melt(cout_geste[COD_PERIODE_SIMPLE %in% c("01","02","03")],
                         id.vars = c("ID_AGREG","GESTE"),measure.vars = c("GAIN","COUT"))

cout_geste_casted = dcast(cout_geste_casted, ID_AGREG ~ GESTE + variable)
setnames(cout_geste_casted,"ID_AGREG","ID_AGREG_GESTE")

besoins_chauffage_init= merge(besoins_chauffage_init, cout_geste_casted, by= "ID_AGREG_GESTE", all.x=T)

cout_geste_casted[cout_geste_casted ==0]

### 
cout_geste[COUT!=0,list(cout_min = min(COUT), cout_moy = mean(COUT, na.rm = T), cout_max = max(COUT)), by="GESTE"]



#### surface moyenne des bâtiments 
surf_moy = fread("table_param_origine/Surf_moy_etab.csv", 
      colClasses = list("character" = c("ID")))

surf_moy[, COD_BRANCHE:=substring(ID, 1,2)]
surf_moy[, COD_SS_BRANCHE:=substring(ID, 3,4)]
surf_moy[, COD_BAT_TYPE:=substring(ID, 5,6)]


#### parc par tranche de consommation de chauffage 

besoins_chauffage_init[,wtd.quantile(ConsoUChauff,weights = `SURFACES 2009`, probs=seq(0,1,0.2))]
besoins_chauffage_init[,wtd.quantile(ConsoUChauff)]
besoins_chauffage_init[,weighted.mean(ConsoUChauff, weights =`SURFACES 2009` )]

besoins_chauffage_init[, Tr_ConsoU := cut(ConsoUChauff,breaks = c(0,50,100,150,200,max(ConsoUChauff)), include.lowest = T, 
                                          labels = c("0-50", "50-100","100-150","150-200",">200"))]


PartTrConsoU = besoins_chauffage_init[,list(Surface = sum(`SURFACES 2009` )), by=c("ID_BRANCHE","Tr_ConsoU")]

#### calcul moyenne gain et coût par geste

listegeste = unique(cout_geste$GESTE)
coltomelt = c(paste(listegeste,"COUT", sep="_"),paste(listegeste,"GAIN", sep="_"))
besoins_chauffage_init = melt(besoins_chauffage_init, measure.vars = coltomelt  )
besoins_chauffage_init[,variable:=as.character(variable)]
besoins_chauffage_init[,TypeVar:= substring(variable, nchar(variable)-3,nchar(variable))]
besoins_chauffage_init[,GESTE := substring(variable,1 ,nchar(variable)-5)]
besoins_chauffage_init[,variable := NULL]

table_coutgain = dcast.data.table(besoins_chauffage_init,... ~ TypeVar )

table_coutgain[, COUT_tot := COUT*`SURFACES 2009`]
table_coutgain[, Conso_tot := BESOIN_U*`SURFACES 2009`/`RDT systeme`]

table_coutgain[, GAIN_tot := GAIN*Conso_tot]

table_coutgain[, Conso_tot_calc:=Conso_tot]
table_coutgain[is.na(GAIN_tot), Conso_tot_calc := NA]
table_coutgain[, Surface_calc := `SURFACES 2009`]
table_coutgain[is.na(COUT_tot), Surface_calc := NA]

##### gains et coûts moyens par branche, conso unitaire et geste

table_coutgain_moy = table_coutgain[, 
                                    list(GAINperc= sum(GAIN_tot, na.rm=T)/sum(Conso_tot_calc, na.rm=T),
                                         GAINunit =  sum(GAIN_tot, na.rm=T)/sum(Surface_calc, na.rm=T),
                                         COUT = sum(COUT_tot, na.rm=T)/sum(Surface_calc, na.rm=T)),
                       by=c("ID_BRANCHE","Tr_ConsoU", "GESTE")]


table_coutgain_moy[GESTE %in% c("ENSMOD","ENSBBC")][order(Tr_ConsoU)]


table_coutgain_moy = melt(table_coutgain_moy, id.vars = c("ID_BRANCHE","Tr_ConsoU", "GESTE"))
table_coutgain_moy = dcast(table_coutgain_moy , ID_BRANCHE + Tr_ConsoU ~ variable+GESTE)

##### gains et coûts moyens par conso unitaire et geste

table_coutgain_moy2 = table_coutgain[, list(GAINperc= sum(GAIN_tot, na.rm=T)/sum(Conso_tot_calc, na.rm=T),
                                            GAINunit =  sum(GAIN_tot, na.rm=T)/sum(Surface_calc, na.rm=T),
                                           COUT = sum(COUT_tot, na.rm=T)/sum(Surface_calc, na.rm=T)),
                                    by=c("Tr_ConsoU", "GESTE")]
table_coutgain_moy2 = melt(table_coutgain_moy2, id.vars = c("Tr_ConsoU", "GESTE"))


table_coutgain_moy2  = dcast(table_coutgain_moy2  , Tr_ConsoU ~ variable+GESTE)

##### gains et coûts moyens par  geste


table_coutgain_moy3 = table_coutgain[, list(GAINperc= sum(GAIN_tot, na.rm=T)/sum(Conso_tot_calc, na.rm=T),
                                            GAINunit =  sum(GAIN_tot, na.rm=T)/sum(Surface_calc, na.rm=T),
                                            COUT = sum(COUT_tot, na.rm=T)/sum(Surface_calc, na.rm=T)),
                                     by=c("GESTE")]

write.table(table_coutgain_moy3, "../AME_AMS_dataDGEC/chroniques investissements/gain_cout_moy_geste.csv",quote = T,row.names = F, sep=";", dec=".")

##### gains et coûts moyens par  niveau du geste

Corresp_geste = data.table(GESTE = unique(cout_geste$GESTE))
Corresp_geste[GESTE %in% c("FENMOD","FENBBC","GTB"), GESTE_DGEC:="Rénovation faible"]
Corresp_geste[GESTE %in% c("FEN_MURBBC","FEN_MURMOD","ENSMOD"), GESTE_DGEC:="Rénovation moyenne"]
Corresp_geste[GESTE %in% c("ENSBBC"), GESTE_DGEC:="Rénovation importante"]

table_coutgain = merge(table_coutgain, Corresp_geste, by="GESTE") 

table_coutgain_moy4 = table_coutgain[, list(GAINperc= sum(GAIN_tot, na.rm=T)/sum(Conso_tot_calc, na.rm=T),
                                            GAINunit =  sum(GAIN_tot, na.rm=T)/sum(Surface_calc, na.rm=T),
                                            COUT = sum(COUT_tot, na.rm=T)/sum(Surface_calc, na.rm=T)),
                                     by=c("GESTE_DGEC")]


table_coutgain_moy5 = table_coutgain[, list(GAINperc= sum(GAIN_tot, na.rm=T)/sum(Conso_tot_calc, na.rm=T),
                                            GAINunit =  sum(GAIN_tot, na.rm=T)/sum(Surface_calc, na.rm=T),
                                            COUT = sum(COUT_tot, na.rm=T)/sum(Surface_calc, na.rm=T)),
                                     by=c("GESTE","ID_BRANCHE")][order(ID_BRANCHE)]


