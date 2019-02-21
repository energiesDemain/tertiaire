
#### liste des fichiers à lire
wbfiles = "table_resultat/publi_dimitri/Scénario AME new décret.xls"
wbfiles[length(wbfiles) +1] = "../_CGDD_Packaging_27.05.15/Result_Excel/resultats_sauvegarde/table_resultat_AMEprixMA3_valeurvertemoins100.xls"
wbfiles[length(wbfiles) +1] = "../_CGDD_Packaging_27.05.15/Result_Excel/resultats_sauvegarde/table_resultat_maintenance_0.xls"
wbfiles[length(wbfiles) +1] = "../_CGDD_Packaging_27.05.15/Result_Excel/resultats_sauvegarde/table_resultat_AME_2016_11_10.xls"

#### noms des scenarios ###
wbname = c("AME")
wbname[length(wbname) +1] = "AME prix MA3 valeur verte"
wbname[length(wbname) +1] = "AME maintenance 0"
wbname[length(wbname) +1] = "AME prix MA3"

#### fonction pour lire les fichiers ###
source("read_resultats_xls.R")
results = lecture_results_modele_tertiaire(wbfiles,wbname =  wbname)
parc = rbindlist(results$parc)
Conso = rbindlist(results$Conso)
etiquette = rbindlist(results$etiquette)
GES = rbindlist(results$GES)
COUTS = rbindlist(results$COUTS)
COUTS_AUTRES = rbindlist(results$COUTS_AUTRES)

source("analyse_param_utilisateurs.R",encoding = "ISO8859-1")
source("graph_param_modele.R",encoding = "ISO8859-1")

#### 

COUTS[, pret_aides := aides + prets + pretsBonifies]
COUTS[, investissementmoinspret_aides := investissement - pret_aides]
COUTS[scenario == "AME prix MA3",]

COUTS[scenario == "AME prix MA3" & investissementmoinspret_aides >100, ]
COUTS[scenario == "AME prix MA3 valeur verte" & investissementmoinspret_aides >100, ]
COUTS[scenario == "AME prix MA3" ,sum(surface), by="typeRenovationSysteme" ]
COUTS[scenario == "AME prix MA3" & typeRenovationBatiment == "ENSBBC" ,sum(surface)/10^6, by="typeRenovationSysteme" ]


summary(COUTS[scenario == "AME prix MA3", investissement - pret_aides])

####### paramètres initiaux coûts intangibles  

cout_intan_geste = fread("table_param_origine/Cout_Intangible_init_geste.csv")

cout_intan_chauff = fread("table_param_origine/Cout_Intangible_init_chauff.csv")

cout_intan_chauff = merge(cout_intan_chauff, COD_SYSTEME_CHAUD[,list(PRODUCTION_CHAUD = SYSTEME_CHAUD,COD_SYSTEME_CHAUD)], by="PRODUCTION_CHAUD", all.x = T)
cout_intan_chauff = merge(cout_intan_chauff, COD_ENERGIE, by="ENERGIE", all.x = T)

summary(cout_intan_chauff)

