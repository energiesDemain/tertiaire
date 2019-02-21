require(knitr)
require(data.table)
require(ggplot2)
require(Hmisc)
require(reshape2)
require(plyr)
require(ggthemes)
require(texreg)

#### liste des fichiers à lire
wbfiles = "table_resultat/publi_dimitri/Scénario AME new décret.xls"
wbfiles[length(wbfiles) +1] = "../_CGDD_Packaging_27.05.15/Result_Excel/resultats_sauvegarde/table_resultat_AMEprixMA3_valeurvertemoins100.xls"
wbfiles[length(wbfiles) +1] = "../_CGDD_Packaging_27.05.15/Result_Excel/resultats_sauvegarde/table_resultat_maintenance_0.xls"

#### noms des scenarios ###
wbname = c("AME")
wbname[length(wbname) +1] = "AME prix MA3 valeur verte"
wbname[length(wbname) +1] = "AME maintenance 0"
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
source("calc_rentabilite_renovation.R",encoding = "UTF8")

tableau_cout_moy_geste
tableau_cout_moy_syst


wb = loadWorkbook("sorties_param_tertiaire/2016_11_28_couts_moyens_modele_tertiaire_BV.xlsx",create = TRUE)

# Create a worksheet
createSheet(wb, name = "gestes")
createSheet(wb, name = "systemes")

# Create a name reference
createName(wb, name = "gestes", formula = "gestes!$B$1")
createName(wb, name = "systemes", formula = "systemes!$B$1")

# Write built-in data.frame
writeNamedRegion(wb,tableau_cout_moy_geste, name = "gestes")
writeNamedRegion(wb,tableau_cout_moy_syst, name =  "systemes")

# Save workbook
saveWorkbook(wb)


