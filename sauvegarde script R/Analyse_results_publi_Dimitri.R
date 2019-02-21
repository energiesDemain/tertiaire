require(data.table)
require(ggplot2)

##### lectures paramètres modèle tertiaire 
source("analyse_param_utilisateurs.R",encoding = "ISO8859-1")

##### thèmes graphiques #####

orange_logement = rgb(0.94,0.51,0)
vert_DD = rgb(0.47,0.70,0.12)
light_vert_DD = rgb(0.47,0.70,0.12) 
light_orange_logement=rgb(0.94,0.51,0) 
violet_transport = rgb(0.35,0.31,0.59)
light_violet_transport= rgb(0.35,0.31,0.59)
turquoise_enerclimat = rgb(0,0.65,0.71)

mytheme_plot <-   theme(legend.background = element_blank(),
                        plot.margin = unit(c("2","2","3","3"),"cm"),
                        plot.background = element_blank(),
                        panel.background = element_blank() ,
                        axis.title.x = element_text(size = 30, vjust =-3),
                        axis.title.y = element_text(size = 30, vjust =3),
                        text = element_text(size = 30),axis.line = element_line(linetype = 1),
                        legend.key = element_blank(), 
                        panel.grid.minor = element_blank(),
                        panel.grid.major.x = element_blank(),
                        panel.margin = unit(0, "cm"), 
                        panel.border = element_rect(color = "black", fill=NA),
                        panel.grid.major.y = element_line(colour = "grey70"),
                        legend.key.height = unit(2, "cm")) 

mytheme_facet_plot <- 
  theme(legend.background = element_blank(),
        plot.margin = unit(c("2","2","3","3"),"cm"),
        plot.background = element_blank(),
        panel.background = element_blank() ,
        axis.title.x = element_text(size = 30, vjust =-3),
        axis.title.y = element_text(size = 30, vjust =3),
        text = element_text(size = 20),axis.line = element_line(linetype = 1),
        legend.key = element_blank(), 
        panel.grid.minor = element_blank(),
        panel.grid.major.x = element_blank(),
        panel.margin = unit(0, "cm"), 
        panel.grid.major.y = element_line(colour = "grey70"),
        legend.key.height = unit(2, "cm"), 
        panel.border = element_rect(color = "black", fill=NA), 
        strip.background = element_rect(color = "black", fill=NA)) 


mytheme_map  = theme(legend.background = element_blank(),plot.margin = unit(c("2","2","3","3"),"cm"),
                     plot.background = element_blank(),panel.background = element_blank() ,
                     axis.title.x = element_text(size = 30, vjust =-3),
                     axis.title.y = element_text(size = 30, vjust =3),
                     panel.grid.major.y = element_line(size =0.5,color="grey80"),
                     panel.grid.minor.y = element_blank(),
                     text = element_text(size = 30),axis.line = element_line(linetype = 1),
                     legend.key.height = unit(2, "cm"), axis.ticks = element_blank(), axis.text=element_blank(),
                     panel.grid = element_blank(), panel.background =element_rect(fill = "white",color = "black"), 
                     legend.position = "right",legend.background = element_rect(fill = "white",color = "black"))


##### lecture sorties du modèle ##### 

#### liste des fichiers à lire
wbfiles = paste0("table_resultat/publi_dimitri/",list.files(path = "table_resultat/publi_dimitri/"))

#wbfiles = "../_CGDD_Packaging_27.05.15/Result_Excel/resultats_sauvegarde/table_resultat_AME_2016_11_10.xls"

#### noms des scenarios ###
wbname = c("AME","décret","demande","offre","ambitieux")


#### fonction pour lire les fichiers ###

source("read_resultats_xls.R")

results = lecture_results_modele_tertiaire(wbfiles,wbname =  wbname)

parc = rbindlist(results$parc)
Conso = rbindlist(results$Conso)
etiquette = rbindlist(results$etiquette)
GES = rbindlist(results$GES)
COUTS = rbindlist(results$COUTS)
COUTS_AUTRES = rbindlist(results$COUTS_AUTRES)
###### graph CONSO ############
Conso = merge(Conso,COD_BRANCHE[,list(nom_branche = BRANCHE, branche = COD_BRANCHE) ], by = "branche")
Conso = merge(Conso,COD_ENERGIE[,list(nom_energie = ENERGIE, energieUsage = COD_ENERGIE) ], by = "energieUsage")

Conso = melt(Conso,id.vars = c("branche","nom_branche","occupation","periodeSimple","energieUsage","nom_energie","usage","usageSimple",
                               "facteurEnergiePrimaire", "scenario"), variable.name = "annee")

##### conso nationale totale
Conso_nationale_tot =  dcast.data.table(Conso, scenario ~ annee ,fun.aggregate = function(x){sum(x)/10^9}, value.var = "value")

##### conso nationale par usage
Conso_nationale_usage = dcast.data.table(Conso, scenario + annee ~usage,fun.aggregate = function(x){sum(x)/10^9}, value.var = "value")



Conso_nationale_usage = melt(Conso_nationale_usage, id.vars = c("scenario","annee"), variable.name = "usage", value.name = "conso_tWhEF")

### calc conso 2009 elec spe ###
Conso_nationale_usage[annee=="2009" & scenario == "AME"]
Conso_nationale_usage[annee=="2009" & scenario == "AME" & usage %in% c("Eclairage","Auxiliaires","Ventilation","Froid_alimentaire",
                                                                       "Process","Bureautique"), sum(conso_tWhEF)]

##### graph conso par usage et scenario

ggplot(Conso_nationale_usage) + geom_line(aes(annee,conso_tWhEF, group = usage, color = usage)) + 
  geom_point(aes(annee,conso_tWhEF,color = usage))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~scenario) + 
  mytheme_facet_plot

ggplot(Conso_nationale_usage) + geom_line(aes(annee,conso_tWhEF, group = scenario, color = scenario)) + 
  geom_point(aes(annee,conso_tWhEF,color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~usage) + 
  mytheme_facet_plot

ggplot(Conso_nationale_usage[usage != "Chauffage"]) + geom_line(aes(annee,conso_tWhEF, group = scenario, color = scenario)) + 
  geom_point(aes(annee,conso_tWhEF,color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~usage) + 
  mytheme_facet_plot

usage_tmp = "Climatisation"

comp_conso_nat_usage = function(usage_tmp){
  ggplot(Conso_nationale_usage[usage ==usage_tmp]) + geom_line(aes(annee,conso_tWhEF, group = scenario, color = scenario)) + 
    geom_point(aes(annee,conso_tWhEF,color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12)) + 
    ggtitle(label = paste("Conso nationale",usage_tmp)) +
    mytheme_facet_plot
}

comp_conso_nat_usage("Chauffage") 
comp_conso_nat_usage("ECS") 
comp_conso_nat_usage("Cuisson") 
comp_conso_nat_usage("Climatisation") 

comp_conso_nat_usage("Ventilation") 
comp_conso_nat_usage("Auxiliaires") 
comp_conso_nat_usage("Eclairage") 
comp_conso_nat_usage("Bureautique") 
comp_conso_nat_usage("Froid_alimentaire") 
comp_conso_nat_usage("Process") 

comp_conso_nat_usage("Autre") 

##### conso par branche et par usage

Conso_branche_usage = dcast.data.table(Conso, scenario + annee+nom_branche~usage,fun.aggregate = sum, value.var = "value")

ggplot(Conso_branche_usage) + geom_line(aes(annee, Chauffage, group = nom_branche, color = nom_branche)) + 
  geom_point(aes(annee,Chauffage, color = nom_branche))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~scenario) + 
  mytheme_facet_plot

ggplot(Conso_branche_usage) + geom_line(aes(annee, Chauffage, group = scenario, color = scenario)) + 
  geom_point(aes(annee,Chauffage, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~nom_branche) + 
  mytheme_facet_plot

###### conso par energie 
Conso_nationale_energie = dcast.data.table(Conso, scenario + annee ~nom_energie,fun.aggregate = sum, value.var = "value")

Conso_nationale_energie = melt(Conso_nationale_energie, id.vars = c("scenario","annee"), variable.name = "energie", value.name = "conso_tWhEF")

ggplot(Conso_nationale_energie ) + geom_line(aes(annee,conso_tWhEF, group = energie, color = energie)) + 
  geom_point(aes(annee,conso_tWhEF,color = energie))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~scenario) + 
  mytheme_facet_plot

comp_conso_nat_energie = function(energie_tmp){
  ggplot(Conso_nationale_energie[energie ==energie_tmp]) + geom_line(aes(annee,conso_tWhEF, group = scenario, color = scenario)) + 
    geom_point(aes(annee,conso_tWhEF,color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12)) + 
    ggtitle(label = paste("Conso nationale",energie_tmp)) +
    mytheme_facet_plot
}

comp_conso_nat_energie("Electricité")
comp_conso_nat_energie("Gaz")
comp_conso_nat_energie("Fioul")
comp_conso_nat_energie("Urbain")
comp_conso_nat_energie("Autres")

##### graph couts des rénovations ####### 

COUTS[typeRenovationBatiment == "Etat initial", COD_SYSTEME_CHAUD := substring(typeRenovationSysteme,14,16)]
COUTS = merge(COUTS, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD", all.x=T)

####### investissement total

####### Investissements par type de rénovation 
INV_SYST_CHAUD = dcast.data.table(COUTS, scenario +annee ~ typeRenovationSysteme, value.var = "investissement", 
                                  fun.aggregate = function(x){sum(x)/10^6})
INV_SYST_CHAUD[,"Etat initial_NP" := NULL]
INV_SYST_CHAUD = melt(INV_SYST_CHAUD,id.vars = c("scenario","annee"))
INV_SYST_CHAUD[, COD_SYSTEME_CHAUD := substring(variable,14,16)]
INV_SYST_CHAUD = merge(INV_SYST_CHAUD, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD")

INV_GESTE = dcast.data.table(COUTS, scenario + annee ~ typeRenovationBatiment, value.var = "investissement", 
                             fun.aggregate = function(x){sum(x)/10^6})

INV_GESTE[,"Etat initial":=NULL]
INV_GESTE = melt(INV_GESTE,id.vars = c("scenario","annee"))

INV_AUTRES_renov = dcast.data.table(COUTS_AUTRES, scenario + annee ~ typeRenovationSysteme, value.var = "investissement", 
                                    fun.aggregate = function(x){sum(x)/10^6})

INV_AUTRES_renov =  melt(INV_AUTRES_renov,id.vars = c("scenario","annee"))


###### plots investissements par type et scenario #####

ggplot(INV_SYST_CHAUD) + geom_line(aes(annee, value, group = scenario, color = scenario)) + 
  geom_point(aes(annee, value, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~SYSTEME_CHAUD) + 
  mytheme_facet_plot

ggplot(INV_SYST_CHAUD) + geom_line(aes(annee, value, group = scenario, color = scenario)) + 
  geom_point(aes(annee, value, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~SYSTEME_CHAUD) + 
  mytheme_facet_plot


ggplot(INV_GESTE) + geom_line(aes(annee, value, group = scenario, color = scenario)) + 
  geom_point(aes(annee, value, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~variable) + 
  mytheme_facet_plot


ggplot(INV_AUTRES_renov) + geom_line(aes(annee, value, group = scenario, color = scenario)) + 
  geom_point(aes(annee, value, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~variable) + 
  mytheme_facet_plot

##### investissements au m2
COUTS[, INV_m2 := investissement/surface]
summary(COUTS)

INV_SYST_CHAUD_m2 = COUTS[, list( investissement = sum(investissement),
                                  surface = sum(surface)), by=c("scenario","annee","SYSTEME_CHAUD")]
INV_SYST_CHAUD_m2[, INV_m2 := investissement/surface]

INV_SYST_CHAUD_m2[scenario == "AME" ,sum(surface)/10^6,by="annee"]


ggplot(INV_SYST_CHAUD_m2[!is.na(SYSTEME_CHAUD)] ) + geom_line(aes(annee, INV_m2, group = scenario, color = scenario)) + 
  geom_point(aes(annee, INV_m2, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~SYSTEME_CHAUD) + 
  mytheme_facet_plot

COUTS_AUTRES[, INV_m2 := investissement/surface]
summary(COUTS_AUTRES[typeRenovationSysteme == "Climatisation"])

INV_AUTRES_renov_m2 = COUTS_AUTRES[, list( investissement = sum(investissement),
                                           surface = sum(surface)), by=c("scenario","annee","typeRenovationSysteme")]
INV_AUTRES_renov_m2[, INV_m2 := investissement/surface]


ggplot(INV_AUTRES_renov_m2) + geom_line(aes(annee, INV_m2, group = scenario, color = scenario)) + 
  geom_point(aes(annee, INV_m2, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~typeRenovationSysteme) + 
  mytheme_facet_plot

##### test clim en 2010 

temp_clim = COUTS_AUTRES[annee == "2010" & typeRenovationSysteme == "Climatisation" & scenario == "AME"]

temp_clim[, list(surface = sum(surface)/10^6, investissement = sum(investissement)/10^6)]
