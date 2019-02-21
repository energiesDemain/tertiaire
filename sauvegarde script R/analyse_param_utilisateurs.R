require(data.table)
require(ggplot2)

##### th?mes graphiques #####
colorpalette = "Spectral"
fillpalette = "Spectral"

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


mytheme_map  = theme(plot.margin = unit(c("2","2","3","3"),"cm"),
                     plot.background = element_blank(),
                     axis.title.x = element_text(size = 30, vjust =-3),
                     axis.title.y = element_text(size = 30, vjust =3),
                     panel.grid.major.y = element_line(size =0.5,color="grey80"),
                     panel.grid.minor.y = element_blank(),
                     text = element_text(size = 30),axis.line = element_line(linetype = 1),
                     legend.key.height = unit(2, "cm"), axis.ticks = element_blank(), axis.text=element_blank(),
                     panel.grid = element_blank(), panel.background =element_rect(fill = "white",color = "black"), 
                     legend.position = "right",legend.background = element_rect(fill = "white",color = "black"))

##### nomenclatures par tertiaire ######
COD_BRANCHE = fread("table_param_origine/Nomenclature_csv/COD_BRANCHE.csv", colClasses = "character")
COD_SS_BRANCHE = fread("table_param_origine/Nomenclature_csv/COD_SS_BRANCHE.csv", colClasses = "character")
COD_BAT_TYPE = fread("table_param_origine/Nomenclature_csv/COD_BAT_TYPE.csv", colClasses = "character")
COD_ENERGIE = fread("table_param_origine/Nomenclature_csv/COD_ENERGIE.csv", colClasses = "character")
COD_OCCUPANT = fread("table_param_origine/Nomenclature_csv/COD_OCCUPANT.csv", colClasses = "character")
COD_PERIODE_DETAIL = fread("table_param_origine/Nomenclature_csv/COD_PERIODE_DETAIL.csv", colClasses = "character")
COD_PERIODE_SIMPLE = fread("table_param_origine/Nomenclature_csv/COD_PERIODE_SIMPLE.csv", colClasses = "character")
COD_SYSTEME_CHAUD = fread("table_param_origine/Nomenclature_csv/COD_SYSTEME_CHAUD.csv", colClasses = "character")
COD_SYSTEME_FROID = fread("table_param_origine/Nomenclature_csv/COD_SYSTEME_FROID.csv", colClasses = "character")
COD_USAGE = fread("table_param_origine/Nomenclature_csv/COD_USAGE.csv", colClasses = "character")
COD_ZONE_CLIMAT = fread("table_param_origine/Nomenclature_csv/COD_ZONE_CLIMAT.csv", colClasses = "character")

##### surface moyenne b?timents #####

surf_moy_bat = fread("table_param_origine/Surf_moy_etab.csv", colClasses =  list("character" = c("BRANCHE","SS_BRANCHE","BAT_TYPE","ID")))
surf_moy_bat[, COD_BRANCHE:=substring(ID, 1,2)]
surf_moy_bat[, COD_SS_BRANCHE:=substring(ID, 3,4)]
surf_moy_bat[, COD_BAT_TYPE:=substring(ID, 5,6)]

##### DONNEES PARC ENTRANT  #####

### conso entrants 

conso_entrants = fread("conso_unitaire_batiments_entrants_AME.csv")
conso_entrants = melt(conso_entrants,id.vars = c("BRANCHE","BAT_TYPE","USAGE"), 
                      variable.name = "période",
                            value.name = "conso_unitaire")

##### calcul consomation totale par m2 pour les usages RT et tous les usages, batiments entrants ##### 

##### Pour le Cepmax : la consommation conventionnelle maximale d'?nergie primaire du b?timent 
##### est limit?e ? 50kWhep/(m2.an) en valeur moyenne. Elle int?gre les cinq usages suivants :
##### chauffage, refroidissement, ?clairage, production d'eau chaude sanitaire, auxiliaires. 
##### Cette valeur est modul?e selon la zone climatique, l'altitude, la surface du b?timent 
##### (pour les seuls b?timents de commerce et les ?tablissements sportifs), 
##### le type de b?timent (climatisable ou pas) et les ?missions de gaz ? effet de serre des ?nergies utilis?es, 
##### pour les solutions renouvelables ? base de bois et les r?seaux de chaleur.

unique(conso_entrants$USAGE)
conso_entrants[ ,USAGE := factor(USAGE, levels = c("Chauffage","Climatisation","Auxiliaires","Ventilation","ECS",
                                                   "Eclairage",
                                                   "Bureautique","Process","Froid_alimentaire","Autre","Cuisson"))]
conso_entrants_usage = dcast.data.table(conso_entrants, BRANCHE + BAT_TYPE + période ~ USAGE,value.var =  "conso_unitaire")
conso_entrants_usage[, conso_RT := sum(Chauffage,Climatisation,Auxiliaires,Ventilation,ECS,Eclairage, na.rm = T), 
                     by= c("BRANCHE","BAT_TYPE","période")]
conso_entrants_usage[, conso_Autres := sum(Bureautique,Process,Froid_alimentaire,Autre,Cuisson, na.rm = T), 
                     by= c("BRANCHE","BAT_TYPE","période")]
conso_entrants_usage[, conso_tot := sum(conso_RT,conso_Autres, na.rm = T), 
                     by= c("BRANCHE","BAT_TYPE","période")]

conso_entrants_usage[duplicated(conso_entrants_usage[, list(BAT_TYPE,période)]),]

##### merge averc les surfaces moyennes des b?timents
conso_entrants_usage = merge(conso_entrants_usage,surf_moy_bat[, 
                             list(BRANCHE, BAT_TYPE,SURFACE)],by= c("BRANCHE","BAT_TYPE" ), all.x = T)


conso_entrants_usage[is.na(SURFACE)]

##### conso unitaire par branche pond?r?e par la surface moyenne
conso_entrants_branche = melt(conso_entrants_usage,id.vars = c("BRANCHE","BAT_TYPE","période","SURFACE"),
                              variable.name = "usage",value.name = "kwhEFm2")

conso_entrants_branche = conso_entrants_branche[, list(kwhEFm2_moy = sum(kwhEFm2*SURFACE)/sum(SURFACE)),by=c("BRANCHE","période","usage")]


ggplot(conso_entrants_branche[usage == "Chauffage"]) + 
  geom_bar(aes(BRANCHE,kwhEFm2_moy, fill=BRANCHE), stat="identity") + 
  mytheme_facet_plot + scale_y_continuous(breaks = seq(0,100,10)) + scale_x_discrete(labels = NULL) +
  facet_grid(~période) + ylab("KwhEF par m?") + theme(axis.ticks.x = element_blank()) + xlab("")
  
ggplot(conso_entrants_branche[usage == "conso_RT"]) + 
  geom_bar(aes(BRANCHE,kwhEFm2_moy, fill=BRANCHE), stat="identity") + 
  mytheme_facet_plot + scale_y_continuous(breaks = seq(0,300,10)) + scale_x_discrete(labels = NULL) +
  facet_grid(~période) + ylab("KwhEF par m?") + theme(axis.ticks.x = element_blank()) + xlab("")

ggplot(conso_entrants_branche[usage == "conso_tot"]) + 
  geom_bar(aes(BRANCHE,kwhEFm2_moy, fill=BRANCHE), stat="identity") + 
  mytheme_facet_plot + scale_y_continuous(breaks = seq(0,300,10)) + scale_x_discrete(labels = NULL) +
  facet_grid(~période) + ylab("KwhEF par m?") + theme(axis.ticks.x = element_blank()) + xlab("")

conso_entrants_branche = merge(conso_entrants_branche, 
                               conso_entrants_branche[période == "Etat de r?f?rence RT2000",
                                                      list(BRANCHE, usage,kWhEFm2_ref = kwhEFm2_moy)], by=c("BRANCHE","usage"))
conso_entrants_branche[,Evol_conso := kwhEFm2_moy/kWhEFm2_ref]

ggplot(conso_entrants_branche[usage == "Chauffage"]) + 
  geom_bar(aes(BRANCHE,Evol_conso, fill=BRANCHE), stat="identity") + 
  mytheme_facet_plot + scale_y_continuous(breaks = seq(0,1,0.1)) + scale_x_discrete(labels = NULL) +
  facet_grid(~période) + ylab("") + theme(axis.ticks.x = element_blank()) + xlab("")

ggplot(conso_entrants_branche[usage == "conso_RT"]) + 
  geom_bar(aes(BRANCHE,Evol_conso, fill=BRANCHE), stat="identity") + 
  mytheme_facet_plot + scale_y_continuous(breaks = seq(0,1,0.1)) + scale_x_discrete(labels = NULL) +
  facet_grid(~période) + ylab("") + theme(axis.ticks.x = element_blank()) + xlab("")

ggplot(conso_entrants_branche[usage == "conso_tot"]) + 
  geom_bar(aes(BRANCHE,Evol_conso, fill=BRANCHE), stat="identity") + 
  mytheme_facet_plot + scale_y_continuous(breaks = seq(0,1,0.1)) + scale_x_discrete(labels = NULL) +
  facet_grid(~période) + ylab("") + theme(axis.ticks.x = element_blank()) + xlab("")

##### DONNEES PARC EXISTANT #####

### chargement  donn?es  parc initial en 2009 ######

parc_init = fread("table_param_origine/Parc_init.csv", colClasses = list("character" =c("ID_AGREG","ID")))

###### ajout noms compr?hensibles 

parc_init[, COD_BRANCHE:=substring(ID, 1,2)]
parc_init[, COD_SS_BRANCHE:=substring(ID, 3,4)]
parc_init[, COD_BAT_TYPE:=substring(ID, 5,6)]
parc_init[, COD_OCCUPANT:=substring(ID, 7,8)]
parc_init[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
parc_init[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
parc_init[, COD_SYSTEME_CHAUD:=substring(ID, 13,14)]
parc_init[, COD_EQ_CLIM:=substring(ID, 15,16)]
parc_init[, COD_ENERGIE :=substring(ID, 17,18)]

parc_init = merge(parc_init, COD_BRANCHE, by="COD_BRANCHE") 
parc_init = merge(parc_init, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
parc_init = merge(parc_init, COD_BAT_TYPE, by="COD_BAT_TYPE") 
parc_init = merge(parc_init, COD_OCCUPANT, by="COD_OCCUPANT") 
parc_init = merge(parc_init, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
parc_init = merge(parc_init, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
parc_init = merge(parc_init, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD") 
#parc_init = merge(parc_init, COD_SYSTEME_FROID, by="COD_SYSTEME_FROID") 
parc_init[COD_EQ_CLIM == "01", EQ_CLIM := "Climatis?"]
parc_init[COD_EQ_CLIM == "02", EQ_CLIM := "Non climatis?"]
parc_init = merge(parc_init, COD_ENERGIE, by="COD_ENERGIE") 

##### ordre des facteurs 
parc_init[, ENERGIE := factor(ENERGIE, levels = c("Electricit?","Gaz","Fioul","Urbain","Autres"))]
unique(parc_init$PERIODE_SIMPLE )
parc_init[, PERIODE_SIMPLE := factor(PERIODE_SIMPLE , levels = c("Av 1980","1981-1998","1999-2008"))]
parc_init[, SYSTEME_CHAUD := factor(SYSTEME_CHAUD , 
                                       levels = c("Chaudi?re gaz",
                                                  "Chaudi?re fioul", 
                                                  "Electrique direct", "PAC",
                                                  "Rooftop", "Tube radiant", "Cassette rayonnante",
                                                  "DRV", "Autre syst?me centralis?", "nr"))]

####### Stats parc initial ########

####### surface totale du parc  initial (895 Mm2)
surf_tot_tertiaire = parc_init[, sum(SURFACES)/10^6]

####### PM des diff?rentes ?nergies 
PM_ENERGIE = parc_init[, list(SurfaceMm2 = sum(SURFACES)/10^6, 
                              PM = sum(SURFACES)/10^6/surf_tot_tertiaire), by="ENERGIE"]

PM_ENERGIE[order(ENERGIE)]

write.table(PM_ENERGIE[order(ENERGIE)],"sorties_param_tertiaire/PM_ENERGIE_CHAUFF_EXISTANT_2009.csv", 
            sep=";",quote = T, dec = ".", row.names = F)

####### PM des diff?rentes ?nergies par branche
PM_BRANCHE = parc_init[,list(SurfaceMm2 = sum(SURFACES)/10^6, 
                             PM_BRANCHE = sum(SURFACES)/10^6/surf_tot_tertiaire), by="BRANCHE"]
write.table(PM_BRANCHE ,"sorties_param_tertiaire/PM_BRANCHE_EXISTANT_2009.csv", 
            sep=";",quote = T, dec = ".", row.names = F)

PM_ENERGIE_BRANCHE = parc_init[, list(SurfaceMm2 = sum(SURFACES)/10^6), 
                              by=c("ENERGIE", "BRANCHE")]

PM_ENERGIE_BRANCHE[,PM_Energie := SurfaceMm2/sum(SurfaceMm2), by="BRANCHE"]
PM_ENERGIE_BRANCHE = PM_ENERGIE_BRANCHE[order(ENERGIE)]
PM_ENERGIE_BRANCHE[,PM_Energie_cum := cumsum(PM_Energie), by="BRANCHE"]
PM_ENERGIE_BRANCHE[,PM_Energie_cum := cumsum(PM_Energie), by="BRANCHE"]

PM_ENERGIE_BRANCHE[order(BRANCHE)]



####### PM des diff?rentes ?nergies par période
PM_PERIODE = parc_init[,list(PM_PERIODE  = sum(SURFACES)/10^6/surf_tot_tertiaire, 
                             SurfaceMm2 = sum(SURFACES)/10^6), by="PERIODE_SIMPLE"]
write.table(PM_PERIODE ,"sorties_param_tertiaire/PM_PERIODE_EXISTANT_2009.csv", 
            sep=";",quote = T, dec = ".", row.names = F)

PM_ENERGIE_PERIODE_SIMPLE = parc_init[, list(SurfaceMm2 = sum(SURFACES)/10^6), 
                                      by=c("ENERGIE", "PERIODE_SIMPLE")]

PM_ENERGIE_PERIODE_SIMPLE[,PM_Energie := SurfaceMm2/sum(SurfaceMm2), by="PERIODE_SIMPLE"]

PM_ENERGIE_PERIODE_SIMPLE = PM_ENERGIE_PERIODE_SIMPLE[order(ENERGIE)]
PM_ENERGIE_PERIODE_SIMPLE[,PM_Energie_cum := cumsum(PM_Energie), by="PERIODE_SIMPLE"]
PM_ENERGIE_PERIODE_SIMPLE[,PM_Energie_cum := cumsum(PM_Energie), by="PERIODE_SIMPLE"]

PM_ENERGIE_PERIODE_SIMPLE[order(PERIODE_SIMPLE)]


####### PM des diff?rentes Syst?mes chauffage 
PM_SYSTEME_CHAUD = parc_init[,list(SurfaceMm2 = sum(SURFACES)/10^6, 
                                   PM_SYSTEME_CHAUD  = sum(SURFACES)/10^6/surf_tot_tertiaire), 
                             by="SYSTEME_CHAUD"]

write.table(PM_SYSTEME_CHAUD ,"sorties_param_tertiaire/PM_SYSTEME_CHAUD_EXISTANT_2009.csv", 
            sep=";",quote = T, dec = ".", row.names = F)

PM_ENERGIE_SYSTEME_CHAUD = parc_init[,list(SurfaceMm2 = sum(SURFACES)/10^6, 
                                   PM_SYSTEME_CHAUD  = sum(SURFACES)/10^6/surf_tot_tertiaire), 
                             by= c("ENERGIE", "SYSTEME_CHAUD")]

write.table(PM_ENERGIE_SYSTEME_CHAUD[order(ENERGIE)] ,"sorties_param_tertiaire/PM_ENERGIE_SYSTEME_CHAUD_EXISTANT_2009.csv", 
            sep=";",quote = T, dec = ".", row.names = F)

PM_ENERGIE_SYSTEME_CHAUD = parc_init[, list(SurfaceMm2 = sum(SURFACES)/10^6), 
                                      by=c("ENERGIE", "SYSTEME_CHAUD")]

PM_ENERGIE_SYSTEME_CHAUD[,PM_Energie := SurfaceMm2/sum(SurfaceMm2), by="SYSTEME_CHAUD"]
PM_ENERGIE_SYSTEME_CHAUD[,PM_SYSTEME_CHAUD := SurfaceMm2/sum(SurfaceMm2), by="ENERGIE"]

PM_ENERGIE_SYSTEME_CHAUD = PM_ENERGIE_SYSTEME_CHAUD[order(SYSTEME_CHAUD)]
PM_ENERGIE_SYSTEME_CHAUD[,PM_Energie_cum := cumsum(PM_Energie), by="SYSTEME_CHAUD"]

PM_ENERGIE_SYSTEME_CHAUD = PM_ENERGIE_SYSTEME_CHAUD[order(ENERGIE)]
PM_ENERGIE_SYSTEME_CHAUD[,PM_SYSTEME_CHAUD_cum := cumsum(PM_SYSTEME_CHAUD), by="ENERGIE"]



PM_ENERGIE_SYSTEME_CHAUD2 = PM_ENERGIE_SYSTEME_CHAUD[order(SYSTEME_CHAUD)]

PM_ENERGIE_SYSTEME_CHAUD2[,PM_SYSTEME_CHAUD_adj := PM_SYSTEME_CHAUD ]
PM_ENERGIE_SYSTEME_CHAUD2[PM_SYSTEME_CHAUD <0.04,PM_SYSTEME_CHAUD_adj := NA]

####### PM clim par branche 

PM_CLIM_BRANCHE = parc_init[,list(SurfaceMm2 = sum(SURFACES)/10^6, 
                                  PM_CLIM_BRANCHE  = sum(SURFACES)/10^6/surf_tot_tertiaire), 
                            by="EQ_CLIM"]

PM_CLIM_BRANCHE = parc_init[, list(SurfaceMm2 = sum(SURFACES)/10^6), 
                               by=c("EQ_CLIM", "BRANCHE")]

PM_CLIM_BRANCHE[,PM_CLIM := SurfaceMm2/sum(SurfaceMm2), by="BRANCHE"]
PM_CLIM_BRANCHE = PM_CLIM_BRANCHE[order(PM_CLIM)]
PM_CLIM_BRANCHE[,PM_CLIM_cum := cumsum(PM_CLIM), by="BRANCHE"]
PM_CLIM_BRANCHE[,PM_CLIM_cum := cumsum(PM_CLIM), by="BRANCHE"]


PM_CLIM_SS_BRANCHE = parc_init[, list(SurfaceMm2 = sum(SURFACES)/10^6), 
                            by=c("EQ_CLIM", "BRANCHE","SS_BRANCHE")]

PM_CLIM_SS_BRANCHE[,PM_CLIM := SurfaceMm2/sum(SurfaceMm2), by="SS_BRANCHE"]
PM_CLIM_SS_BRANCHE = PM_CLIM_SS_BRANCHE[order(PM_CLIM)]
PM_CLIM_SS_BRANCHE[,PM_CLIM_cum := cumsum(PM_CLIM), by="SS_BRANCHE", ]
PM_CLIM_SS_BRANCHE[,PM_CLIM_cum := cumsum(PM_CLIM), by="SS_BRANCHE"]
PM_CLIM_SS_BRANCHE = PM_CLIM_SS_BRANCHE[order(BRANCHE),]

##### DONNEES COUTs DES GESTES #####

cout_geste = fread("table_param_origine/Bibli_geste_bati.csv", 
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

##### DONNEES Rendements et  couts syst?mes de chauffage #####

cout_rdt_chauff = fread("table_param_origine/Bibli_rdt_chauff.csv", 
                        colClasses = list("character" = c("ID_AGREG","PERIODE")))

cout_rdt_chauff[, COD_BRANCHE:=substring(ID_AGREG, 1,2)]
cout_rdt_chauff[, COD_SS_BRANCHE:=substring(ID_AGREG, 3,4)]
cout_rdt_chauff[, COD_BAT_TYPE:=substring(ID_AGREG, 5,6)]
cout_rdt_chauff[, COD_SYSTEME_CHAUD:=substring(ID_AGREG, 7,8)]
cout_rdt_chauff[, COD_ENERGIE :=substring(ID_AGREG, 9,10)]

setkeyv(cout_rdt_chauff,c("BRANCHE","SS_BRANCHE", "BAT_TYPE"))
ordre = unique(cout_rdt_chauff$SS_BRANCHE)
cout_rdt_chauff[, SS_BRANCHE:=factor(SS_BRANCHE, levels = ordre )]
cout_rdt_chauff[, PERIODE :=factor(PERIODE)]
cout_rdt_chauff[, PERIODE:=relevel(PERIODE, ref = "0")]
cout_rdt_chauff[, PRODUCTION_CHAUD := 
                  factor(PRODUCTION_CHAUD, 
                         levels =c("Chaudi?re gaz","Chaudi?re condensation gaz",
                                   "Chaudi?re fioul","Chaudi?re condensation fioul",
                                   "Electrique direct","Electrique direct performant",
                                   "PAC","PAC performant",
                                   "Rooftop", "Rooftop performant",
                                   "Tube radiant", "Tube radiant performant" ,
                                   "Cassette rayonnante","Cassette rayonnante performant",
                                   "DRV", "DRV performant",
                                   "Autre syst?me centralis?",  "Autre syst?me centralis? performant",
                                   "nr"))]
unique(cout_rdt_chauff$PRODUCTION_CHAUD)
summary(as.factor(cout_rdt_chauff$PRODUCTION_CHAUD))

nom_syst_non_performant =c("Chaudi?re gaz", "Chaudi?re fioul", "Electrique direct", "PAC",
                           "Rooftop", "Tube radiant", "Cassette rayonnante","DRV", 
                           "Autre syst?me centralis?")


###### incoh?rences entre ID Agreg et colonnes individuelles de la nomenclature 
cout_rdt_chauff_check = merge(cout_rdt_chauff, COD_BRANCHE, by="COD_BRANCHE") 
cout_rdt_chauff_check = merge(cout_rdt_chauff_check, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
cout_rdt_chauff_check = merge(cout_rdt_chauff_check, COD_BAT_TYPE, by="COD_BAT_TYPE") 
cout_rdt_chauff_check = merge(cout_rdt_chauff_check, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD") 
cout_rdt_chauff_check = merge(cout_rdt_chauff_check, COD_ENERGIE, by="COD_ENERGIE") 

cout_rdt_chauff_check[BRANCHE.x != BRANCHE.y]
cout_rdt_chauff_check[SS_BRANCHE.x != SS_BRANCHE.y]
cout_rdt_chauff_check[BAT_TYPE.x != BAT_TYPE.y, ]
cout_rdt_chauff_check[BAT_TYPE.x != BAT_TYPE.y, list(BAT_TYPE.x,BAT_TYPE.y)]
cout_rdt_chauff_check[SYSTEME_CHAUD != PRODUCTION_CHAUD, list(SYSTEME_CHAUD,PRODUCTION_CHAUD)]
cout_rdt_chauff_check[ENERGIE.x != ENERGIE.y, list(ENERGIE.x,ENERGIE.y)]
toto = cout_rdt_chauff_check[ENERGIE.x != ENERGIE.y,]


toto = toto[,list(SYSTEME_CHAUD, RDT)]

toto = unique(cout_rdt_chauff[,list(BAT_TYPE,COD_BAT_TYPE)])[order(COD_BAT_TYPE)]
toto[BAT_TYPE %in% toto[duplicated(BAT_TYPE),BAT_TYPE]]

##### DONNEES Rendements et couts climatisation #####

cout_rdt_clim = fread("table_param_origine/Bibli_rdt_clim.csv", 
                      colClasses = list("character" = c("ID_AGREG","PERIODE")))

cout_rdt_clim[, COD_BRANCHE:=substring(ID_AGREG, 1,2)]
cout_rdt_clim[, COD_SS_BRANCHE:=substring(ID_AGREG, 3,4)]
cout_rdt_clim[, COD_BAT_TYPE:=substring(ID_AGREG, 5,6)]
cout_rdt_clim[, COD_EQ_CLIM:=substring(ID_AGREG, 7)]
cout_rdt_clim[, PERIODE_LONG := factor(PERIODE, levels = c("0","1","2","3","4","5"), 
                                       labels = c("2009","2010-2015", "2015-2020","2020-2030","2030-2040","2040-2050"))]

# cout_rdt_clim  = merge(cout_rdt_clim , COD_BRANCHE, by="COD_BRANCHE") 
# cout_rdt_clim  = merge(cout_rdt_clim , COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
# cout_rdt_clim  = merge(cout_rdt_clim , COD_BAT_TYPE, by="COD_BAT_TYPE") 
# 
# cout_rdt_clim[BRANCHE.x != BRANCHE.y]
# cout_rdt_clim[SS_BRANCHE.x != SS_BRANCHE.y]
# cout_rdt_clim[BAT_TYPE.x != BAT_TYPE.y, list(BAT_TYPE.x,BAT_TYPE.y)]

##### stats sur les co?ts 

cout_rdt_clim[, mean(COUT)]
cout_rdt_clim[, mean(RDT)]

####### param?tres initiaux co?ts intangibles  

cout_intan_geste = fread("table_param_origine/Cout_Intangible_init_geste.csv")

cout_intan_chauff = fread("table_param_origine/Cout_Intangible_init_chauff.csv")

cout_intan_chauff = merge(cout_intan_chauff, COD_SYSTEME_CHAUD[,list(PRODUCTION_CHAUD = SYSTEME_CHAUD,COD_SYSTEME_CHAUD)], by="PRODUCTION_CHAUD", all.x = T)
cout_intan_chauff = merge(cout_intan_chauff, COD_ENERGIE, by="ENERGIE", all.x = T)

summary(cout_intan_chauff)

  
##### DONNEES BESOINS INITIAUX PAR USAGE #####

###### Chauffage ######

besoins_chauff = fread("table_param_origine/Chauffage_init.csv",
                       colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_chauff[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_chauff[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_chauff[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_chauff[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_chauff[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_chauff[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_chauff[, COD_SYSTEME_CHAUD:=substring(ID, 13,14)]
besoins_chauff[, COD_EQ_CLIM :=substring(ID, 15,16)]
besoins_chauff[, COD_ENERGIE :=substring(ID, 17,18)]

besoins_chauff = merge(besoins_chauff, COD_BRANCHE, by="COD_BRANCHE") 
besoins_chauff = merge(besoins_chauff, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_chauff = merge(besoins_chauff, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_chauff = merge(besoins_chauff, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_chauff = merge(besoins_chauff, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_chauff = merge(besoins_chauff, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_chauff = merge(besoins_chauff, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD") 
besoins_chauff = merge(besoins_chauff, COD_ENERGIE, by="COD_ENERGIE") 

summary(besoins_chauff)

###### Climatisation ######

besoins_clim = fread("table_param_origine/Climatisation_init.csv",
                     colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_clim[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_clim[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_clim[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_clim[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_clim[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_clim[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_clim[, COD_EQ_CLIM :=substring(ID, 13,14)]
besoins_clim[, COD_ENERGIE :=substring(ID, 15,16)]

besoins_clim = merge(besoins_clim, COD_BRANCHE, by="COD_BRANCHE") 
besoins_clim = merge(besoins_clim, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_clim = merge(besoins_clim, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_clim = merge(besoins_clim, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_clim = merge(besoins_clim, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_clim = merge(besoins_clim, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_clim = merge(besoins_clim, COD_ENERGIE, by="COD_ENERGIE") 

###### auxiliaires ######

besoins_auxiliaires = fread("table_param_origine/Auxiliaires_init.csv",
                            colClasses = list("character" = c("ID_AGREG","ID")))

besoins_auxiliaires[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_auxiliaires[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_auxiliaires[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_auxiliaires[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_auxiliaires[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_auxiliaires[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_auxiliaires[, COD_SYSTEME_CHAUD:=substring(ID, 13,14)]
besoins_auxiliaires[, COD_EQ_CLIM :=substring(ID, 15,16)]
besoins_auxiliaires[, COD_ENERGIE :=substring(ID, 17,18)]

besoins_auxiliaires = merge(besoins_auxiliaires, COD_BRANCHE, by="COD_BRANCHE") 
besoins_auxiliaires = merge(besoins_auxiliaires, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_auxiliaires = merge(besoins_auxiliaires, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_auxiliaires = merge(besoins_auxiliaires, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_auxiliaires = merge(besoins_auxiliaires, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_auxiliaires = merge(besoins_auxiliaires, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_auxiliaires = merge(besoins_auxiliaires, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD") 
besoins_auxiliaires = merge(besoins_auxiliaires, COD_ENERGIE, by="COD_ENERGIE") 

ratio_aux_chaud = fread("table_param_origine/ratio_aux_chaud.csv", colClasses = c("SYS_CHAUD"="character"))
ratio_aux_froid = fread("table_param_origine/Ratio_aux_froid.csv", colClasses = c("SYS_FROID"="character"))

besoins_auxiliaires = merge(besoins_auxiliaires, ratio_aux_chaud[,list(COD_SYSTEME_CHAUD = SYS_CHAUD,RATIO_AUX_CHAUD =RATIO)], 
                            by="COD_SYSTEME_CHAUD", all.x = T)

besoins_chauff = merge(besoins_chauff, ratio_aux_chaud[,list(COD_SYSTEME_CHAUD = SYS_CHAUD,RATIO_AUX_CHAUD =RATIO)], 
                            by="COD_SYSTEME_CHAUD", all.x = T)

ratio_aux_froid[SYS_FROID == "01",RATIO := 0.0278]
ratio_aux_froid[SYS_FROID == "02",RATIO := 0]

besoins_chauff = merge(besoins_chauff, ratio_aux_froid[,list(COD_EQ_CLIM= SYS_FROID,RATIO_AUX_FROID =RATIO)], 
                       by="COD_EQ_CLIM", all.x = T)


###### ventilation ######

besoins_ventilation = fread("table_param_origine/Ventilation_init.csv",
                            colClasses = list("character" = c("ID_AGREG","ID")))

besoins_ventilation[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_ventilation[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_ventilation[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_ventilation[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_ventilation[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_ventilation[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_ventilation[, COD_SYSTEME_CHAUD:=substring(ID, 13,14)]
besoins_ventilation[, COD_EQ_CLIM :=substring(ID, 15,16)]
besoins_ventilation[, COD_ENERGIE :=substring(ID, 17,18)]

besoins_ventilation = merge(besoins_ventilation, COD_BRANCHE, by="COD_BRANCHE") 
besoins_ventilation = merge(besoins_ventilation, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_ventilation = merge(besoins_ventilation, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_ventilation = merge(besoins_ventilation, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_ventilation = merge(besoins_ventilation, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_ventilation = merge(besoins_ventilation, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_ventilation = merge(besoins_ventilation, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD") 
besoins_ventilation = merge(besoins_ventilation, COD_ENERGIE, by="COD_ENERGIE") 

###### eclairage ######

besoins_eclairage = fread("table_param_origine/Eclairage_init.csv",
                          colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_eclairage[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_eclairage[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_eclairage[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_eclairage[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_eclairage[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_eclairage[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_eclairage[, COD_ENERGIE :=substring(ID, 13,14)]

besoins_eclairage = merge(besoins_eclairage, COD_BRANCHE, by="COD_BRANCHE") 
besoins_eclairage = merge(besoins_eclairage, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_eclairage = merge(besoins_eclairage, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_eclairage = merge(besoins_eclairage, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_eclairage = merge(besoins_eclairage, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_eclairage = merge(besoins_eclairage, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_eclairage = merge(besoins_eclairage, COD_ENERGIE, by="COD_ENERGIE") 

###### ECS ##########

besoins_ECS = fread("table_param_origine/ECS_init.csv",
                    colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_ECS[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_ECS[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_ECS[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_ECS[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_ECS[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_ECS[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_ECS[, COD_ENERGIE :=substring(ID, 13,14)]

besoins_ECS = merge(besoins_ECS, COD_BRANCHE, by="COD_BRANCHE") 
besoins_ECS = merge(besoins_ECS, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_ECS = merge(besoins_ECS, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_ECS = merge(besoins_ECS, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_ECS = merge(besoins_ECS, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_ECS = merge(besoins_ECS, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_ECS = merge(besoins_ECS, COD_ENERGIE, by="COD_ENERGIE") 

rdt_ECS = fread("table_param_origine/rdt_ecs.csv")

###### Cuisson ##########

besoins_cuisson = fread("table_param_origine/Cuisson_init.csv",
                        colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_cuisson[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_cuisson[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_cuisson[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_cuisson[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_cuisson[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_cuisson[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_cuisson[, COD_ENERGIE :=substring(ID, 13,14)]

besoins_cuisson = merge(besoins_cuisson, COD_BRANCHE, by="COD_BRANCHE") 
besoins_cuisson = merge(besoins_cuisson, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_cuisson = merge(besoins_cuisson, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_cuisson = merge(besoins_cuisson, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_cuisson = merge(besoins_cuisson, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_cuisson = merge(besoins_cuisson, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_cuisson = merge(besoins_cuisson, COD_ENERGIE, by="COD_ENERGIE") 

####### froid alimentaire #####

besoins_froid = fread("table_param_origine/Froid_alimentaire_init.csv",
                      colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_froid[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_froid[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_froid[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_froid[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_froid[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_froid[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_froid[, COD_ENERGIE :=substring(ID, 13,14)]

besoins_froid = merge(besoins_froid, COD_BRANCHE, by="COD_BRANCHE") 
besoins_froid = merge(besoins_froid, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_froid = merge(besoins_froid, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_froid = merge(besoins_froid, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_froid = merge(besoins_froid, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_froid = merge(besoins_froid, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_froid = merge(besoins_froid, COD_ENERGIE, by="COD_ENERGIE") 

####### bureautique #######

besoins_bureautique = fread("table_param_origine/Bureautique_init.csv",
                     colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_bureautique[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_bureautique[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_bureautique[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_bureautique[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_bureautique[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_bureautique[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_bureautique[, COD_ENERGIE :=substring(ID, 13,14)]

besoins_bureautique = merge(besoins_bureautique, COD_BRANCHE, by="COD_BRANCHE") 
besoins_bureautique = merge(besoins_bureautique, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_bureautique = merge(besoins_bureautique, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_bureautique = merge(besoins_bureautique, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_bureautique = merge(besoins_bureautique, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_bureautique = merge(besoins_bureautique, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_bureautique = merge(besoins_bureautique, COD_ENERGIE, by="COD_ENERGIE") 

####### process #######

besoins_process = fread("table_param_origine/Process_init.csv",
                      colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_process[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_process[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_process[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_process[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_process[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_process[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_process[, COD_ENERGIE :=substring(ID, 13,14)]

besoins_process = merge(besoins_process, COD_BRANCHE, by="COD_BRANCHE") 
besoins_process = merge(besoins_process, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_process = merge(besoins_process, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_process = merge(besoins_process, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_process = merge(besoins_process, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_process = merge(besoins_process, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_process = merge(besoins_process, COD_ENERGIE, by="COD_ENERGIE") 

####### autres besoins #######

besoins_autres = fread("table_param_origine/Autre_init.csv",
                        colClasses = list("character" = c("ID_AGREG","ID") ))

besoins_autres[, COD_BRANCHE:=substring(ID, 1,2)]
besoins_autres[, COD_SS_BRANCHE:=substring(ID, 3,4)]
besoins_autres[, COD_BAT_TYPE:=substring(ID, 5,6)]
besoins_autres[, COD_OCCUPANT:=substring(ID, 7,8)]
besoins_autres[, COD_PERIODE_DETAIL :=substring(ID, 9,10)]
besoins_autres[, COD_PERIODE_SIMPLE:=substring(ID, 11,12)]
besoins_autres[, COD_ENERGIE :=substring(ID, 13,14)]

besoins_autres = merge(besoins_autres, COD_BRANCHE, by="COD_BRANCHE") 
besoins_autres = merge(besoins_autres, COD_SS_BRANCHE, by="COD_SS_BRANCHE") 
besoins_autres = merge(besoins_autres, COD_BAT_TYPE, by="COD_BAT_TYPE") 
besoins_autres = merge(besoins_autres, COD_OCCUPANT, by="COD_OCCUPANT") 
besoins_autres = merge(besoins_autres, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL") 
besoins_autres = merge(besoins_autres, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE") 
besoins_autres = merge(besoins_autres, COD_ENERGIE, by="COD_ENERGIE") 


###### RECONSTITUTION DES CONSO UNITAIRES DU PARC INITIAL #######

###### besoins chauffage, ventilation et auxiliaires par energie

besoins_chauff_Energie = dcast(besoins_chauff,ID~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_chauff_Energie,c("ID",paste0("BESOIN_chauff_",c("01","02","03","04","06"))))

besoins_auxiliaires_Energie = dcast(besoins_auxiliaires,ID~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_auxiliaires_Energie,c("ID",paste0("BESOIN_auxiliaires_",c("01","02","03","04","06"))))
besoins_ventilation_Energie = dcast(besoins_chauff,ID~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_ventilation_Energie,c("ID",paste0("BESOIN_ventilation_",c("01","02","03","04","06"))))

besoins_auxiliaires[,sum(BESOIN, na.rm=T)/10^9]

###### merge parc avec besoins de chauffage, auxiliaires, ventilation et rdt #########

besoins_parc_init = merge(parc_init, 
                          besoins_chauff[,list(ID,BESOIN_CHAUFF = BESOIN,  RATIO_AUX_CHAUD,  RATIO_AUX_FROID)],by="ID")
besoins_parc_init = merge(besoins_parc_init, besoins_chauff_Energie,by="ID")

besoins_parc_init = merge(besoins_parc_init , besoins_auxiliaires[,list(ID,BESOIN_AUXILIAIRES = BESOIN)],by="ID")
besoins_parc_init = merge(besoins_parc_init , besoins_auxiliaires_Energie,by="ID")

besoins_parc_init = merge(besoins_parc_init , besoins_ventilation[,list(ID,BESOIN_VENTILATION = BESOIN)],by="ID")
besoins_parc_init = merge(besoins_parc_init , besoins_ventilation_Energie,by="ID")

#### ajouts co?ts et rdt chauffage

besoins_parc_init = merge(besoins_parc_init , 
                          cout_rdt_chauff[PERIODE == "0" & PRODUCTION_CHAUD %in% c(nom_syst_non_performant,"nr")  ,
                                          list(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                               COD_SYSTEME_CHAUD,COD_ENERGIE,
                                               RDT, COUT, CEE)],
                          by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                               "COD_SYSTEME_CHAUD","COD_ENERGIE"), all.x = T)

##### parc sans cout et rendement associ? (li? ? un bug sur la table bibli_rdt_chauff) #####

besoins_parc_init[is.na(COUT), sum(SURFACES)/10^6]
unique(besoins_parc_init[is.na(COUT), BAT_TYPE])
unique(besoins_parc_init[is.na(COUT), SS_BRANCHE])
unique(besoins_parc_init[is.na(COUT), BRANCHE])

cout_rdt_chauff[BRANCHE == "Transport" & PERIODE == "0" ]
cout_rdt_chauff[BAT_TYPE == "Petit Hotel sans restaurant Haut de gamme" & PERIODE == "0" ]

besoins_parc_init[is.na(RDT) & BAT_TYPE == "Petit Hotel sans restaurant Haut de gamme",COD_BAT_TYPE]

#### test calc conso chauffage totale 
besoins_chauff[, sum(BESOIN)/10^9]
besoins_parc_init[, sum(BESOIN_CHAUFF)/10^9]
besoins_parc_init[!is.na(RDT), sum(BESOIN_CHAUFF)/10^9]
besoins_parc_init[, sum(BESOIN_CHAUFF/RDT, na.rm=T)/10^9]

besoins_parc_init[, sum(BESOIN_VENTILATION)/10^9]
besoins_parc_init[, sum(BESOIN_AUXILIAIRES)/10^9]

besoins_parc_init[, sum(BESOIN_CHAUFF, BESOIN_VENTILATION,BESOIN_AUXILIAIRES)/10^9]

besoins_parc_init[, CONSO_CHAUFF := BESOIN_CHAUFF/RDT]
besoins_parc_init[ ,CONSO_CHAUFF_EP := 
                     sum(BESOIN_chauff_01,BESOIN_chauff_03,BESOIN_chauff_04,BESOIN_chauff_06,BESOIN_chauff_02*2.58, na.rm = T)/RDT, 
                   by ="ID"]

#### calc auxiliaires avec les ratios

besoins_parc_init[,BESOIN_AUXILIAIRES_CALC := BESOIN_CHAUFF*RATIO_AUX_CHAUD]


###### aggreg sur tous les syst?mes chauds  et ?nergie de chauff 
besoins_agreg = besoins_parc_init[, list(SURFACES = sum(SURFACES, na.rm=T),
                                         BESOIN_CHAUFF = sum(BESOIN_CHAUFF, na.rm=T), 
                                         BESOIN_AUXILIAIRES_CALC =sum(BESOIN_AUXILIAIRES_CALC , na.rm=T),
                                         CONSO_CHAUFF = sum(CONSO_CHAUFF, na.rm=T),
                                         CONSO_CHAUFF_EP = sum(CONSO_CHAUFF_EP, na.rm=T),
                                         BESOIN_AUXILIAIRES = sum(BESOIN_AUXILIAIRES, na.rm=T),
                                         BESOIN_VENTILATION = sum(BESOIN_VENTILATION, na.rm=T)), 
                                  by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                                                 "COD_OCCUPANT", "COD_PERIODE_DETAIL",
                                                 "COD_PERIODE_SIMPLE", "COD_EQ_CLIM", 
                                                 "BRANCHE","SS_BRANCHE","BAT_TYPE",
                                                 "OCCUPANT", "PERIODE_DETAIL",
                                                 "PERIODE_SIMPLE", "EQ_CLIM")]



##### ajouts des besoins de climatisation

besoins_agreg[, ID_CLIM := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                  COD_OCCUPANT, COD_PERIODE_DETAIL,
                                  COD_PERIODE_SIMPLE, COD_EQ_CLIM,"02")]

besoins_agreg  = merge(besoins_agreg  , besoins_clim[,list(ID_CLIM = ID,BESOIN_CLIM = BESOIN)],by="ID_CLIM")


###### ajouts rdt climatisation 
besoins_agreg = merge(besoins_agreg , 
                      cout_rdt_clim[PERIODE == "0",
                                          list(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                               COD_EQ_CLIM,
                                               RDT)],
                          by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                               "COD_EQ_CLIM"), all.x = T)

#### test calc conso climatisation totale 
besoins_agreg[is.na(RDT)]
besoins_agreg[, sum(BESOIN_CLIM)/10^9]
besoins_agreg[RDT!=0, sum(BESOIN_CLIM/RDT)/10^9]

besoins_agreg[RDT!=0, CONSO_CLIM := BESOIN_CLIM/RDT]
besoins_agreg[RDT==0, CONSO_CLIM := 0]

##### ajouts besoins auxiliaires de clim 

besoins_agreg[COD_EQ_CLIM == "01",BESOIN_AUXILIAIRES_CALC2 := BESOIN_AUXILIAIRES_CALC + BESOIN_CLIM*0.0278]
besoins_agreg[COD_EQ_CLIM == "02",BESOIN_AUXILIAIRES_CALC2 := BESOIN_AUXILIAIRES_CALC]

besoins_agreg[, sum(BESOIN_AUXILIAIRES_CALC2)/10^9]
besoins_agreg[, sum(BESOIN_AUXILIAIRES_CALC)/10^9]


##### ajouts conso clim unitaires au parc de base 


besoins_parc_init = merge(besoins_parc_init, besoins_agreg[,
                                                           list(CONSOU_CLIM =CONSO_CLIM/SURFACES,
                                                                CONSOU_CLIM_EP =CONSO_CLIM*2.58/SURFACES), 
                                                           by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                                                           "COD_OCCUPANT", "COD_PERIODE_DETAIL",
                                                           "COD_PERIODE_SIMPLE","COD_EQ_CLIM")],
                          by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                               "COD_OCCUPANT", "COD_PERIODE_DETAIL",
                               "COD_PERIODE_SIMPLE","COD_EQ_CLIM"),all.x =T)

summary(besoins_parc_init$CONSOU_CLIM_EP)
        
##### aggreg sur les surfaces climatis?s ou non #####

besoins_agreg2 = besoins_agreg[, list(SURFACES = sum(SURFACES),
                                      BESOIN_CHAUFF = sum(BESOIN_CHAUFF, na.rm=T), 
                                      CONSO_CHAUFF = sum(CONSO_CHAUFF, na.rm=T),
                                      CONSO_CHAUFF_EP = sum(CONSO_CHAUFF_EP, na.rm=T),
                                     BESOIN_AUXILIAIRES = sum(BESOIN_AUXILIAIRES, na.rm=T),
                                     BESOIN_AUXILIAIRES_CALC = sum(BESOIN_AUXILIAIRES_CALC, na.rm=T),
                                     BESOIN_AUXILIAIRES_CALC_EP = sum(BESOIN_AUXILIAIRES_CALC, na.rm=T)*2.58,
                                     BESOIN_VENTILATION = sum(BESOIN_VENTILATION, na.rm=T),
                                     BESOIN_VENTILATION_EP = sum(BESOIN_VENTILATION, na.rm=T)*2.58,
                                     BESOIN_CLIM = sum(BESOIN_CLIM, na.rm=T), 
                                     CONSO_CLIM = sum(CONSO_CLIM, na.rm=T), 
                                     CONSO_CLIM_EP = sum(CONSO_CLIM, na.rm=T)*2.58), 
                                  by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                                       "COD_OCCUPANT", "COD_PERIODE_DETAIL",
                                       "COD_PERIODE_SIMPLE", 
                                       "BRANCHE","SS_BRANCHE","BAT_TYPE",
                                       "OCCUPANT", "PERIODE_DETAIL",
                                       "PERIODE_SIMPLE")]


##### ajouts des besoins autres usages #####

besoins_agreg2[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                  COD_OCCUPANT, COD_PERIODE_DETAIL,
                                  COD_PERIODE_SIMPLE)]

besoins_ECS[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                    COD_OCCUPANT, COD_PERIODE_DETAIL,
                                    COD_PERIODE_SIMPLE)]

besoins_ECS = merge(besoins_ECS, rdt_ECS[,list(BRANCHE,ENERGIE,RDT_ECS = ref2009)],by=c("BRANCHE","ENERGIE"), all.x=T )
besoins_ECS[, CONSO_ECS := BESOIN/RDT_ECS]
besoins_ECS[COD_ENERGIE == "02", CONSO_ECS_EP := CONSO_ECS*2.58]
besoins_ECS[COD_ENERGIE != "02", CONSO_ECS_EP := CONSO_ECS]

besoins_cuisson[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                    COD_OCCUPANT, COD_PERIODE_DETAIL,
                                    COD_PERIODE_SIMPLE)]

besoins_cuisson[COD_ENERGIE == "02", BESOIN_EP := BESOIN*2.58]
besoins_cuisson[COD_ENERGIE != "02", BESOIN_EP := BESOIN]



besoins_process[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                        COD_OCCUPANT, COD_PERIODE_DETAIL,
                                        COD_PERIODE_SIMPLE)]
besoins_process[COD_ENERGIE == "02", BESOIN_EP := BESOIN*2.58]
besoins_process[COD_ENERGIE != "02", BESOIN_EP := BESOIN]


besoins_bureautique[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                        COD_OCCUPANT, COD_PERIODE_DETAIL,
                                        COD_PERIODE_SIMPLE)]
besoins_bureautique[COD_ENERGIE == "02", BESOIN_EP := BESOIN*2.58]
besoins_bureautique[COD_ENERGIE != "02", BESOIN_EP := BESOIN]

besoins_eclairage[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                        COD_OCCUPANT, COD_PERIODE_DETAIL,
                                        COD_PERIODE_SIMPLE)]
besoins_eclairage[COD_ENERGIE == "02", BESOIN_EP := BESOIN*2.58]
besoins_eclairage[COD_ENERGIE != "02", BESOIN_EP := BESOIN]

besoins_froid[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                          COD_OCCUPANT, COD_PERIODE_DETAIL,
                                          COD_PERIODE_SIMPLE)]
besoins_froid[COD_ENERGIE == "02", BESOIN_EP := BESOIN*2.58]
besoins_froid[COD_ENERGIE != "02", BESOIN_EP := BESOIN]


besoins_autres[, ID_AUUSAGES := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE,
                                      COD_OCCUPANT, COD_PERIODE_DETAIL,
                                      COD_PERIODE_SIMPLE)]

besoins_autres[COD_ENERGIE == "02", BESOIN_EP := BESOIN*2.58]
besoins_autres[COD_ENERGIE != "02", BESOIN_EP := BESOIN]


besoins_ECS_Energie = dcast.data.table(besoins_ECS,ID_AUUSAGES~COD_ENERGIE,value.var = "BESOIN" )

setnames(besoins_ECS_Energie,c("ID_AUUSAGES",paste0("BESOIN_ECS_",c("01","02","03","04","06"))))
besoins_cuisson_Energie = dcast.data.table(besoins_cuisson,ID_AUUSAGES~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_cuisson_Energie,c("ID_AUUSAGES",paste0("BESOIN_CUISSON_",c("01","02","03","04"))))
besoins_process_Energie =  dcast.data.table(besoins_process,ID_AUUSAGES~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_process_Energie,c("ID_AUUSAGES",paste0("BESOIN_PROCESS_",c("02"))))
besoins_bureautique_Energie =  dcast.data.table(besoins_bureautique,ID_AUUSAGES~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_bureautique_Energie,c("ID_AUUSAGES",paste0("BESOIN_BUREAUTIQUE_",c("02"))))
besoins_eclairage_Energie =  dcast.data.table(besoins_eclairage,ID_AUUSAGES~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_eclairage_Energie,c("ID_AUUSAGES",paste0("BESOIN_ECLAIRAGE_",c("02"))))
besoins_froid_Energie =  dcast.data.table(besoins_froid,ID_AUUSAGES~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_froid_Energie,c("ID_AUUSAGES",paste0("BESOIN_FROID_",c("02"))))
besoins_autres_Energie =  dcast.data.table(besoins_autres,ID_AUUSAGES~COD_ENERGIE,value.var = "BESOIN" )
setnames(besoins_autres_Energie,c("ID_AUUSAGES",paste0("BESOIN_AUTRES_",c("01","02","03","04"))))



besoins_ECS[ID_AUUSAGES %in% besoins_agreg2$ID_AUUSAGES == F]

besoins_agreg2 = merge(besoins_agreg2 , besoins_ECS[,list(BESOIN_ECS = sum(BESOIN), CONSO_ECS = sum(CONSO_ECS),  CONSO_ECS_EP = sum(CONSO_ECS_EP)),by="ID_AUUSAGES"],by="ID_AUUSAGES", all.x=T)
besoins_agreg2 = merge(besoins_agreg2 , besoins_cuisson[,list(BESOIN_CUISSON = sum(BESOIN), BESOIN_CUISSON_EP = sum(BESOIN_EP)),by="ID_AUUSAGES"],by="ID_AUUSAGES", all.x=T)
besoins_agreg2 = merge(besoins_agreg2 , besoins_process[,list(BESOIN_PROCESS = sum(BESOIN), BESOIN_PROCESS_EP = sum(BESOIN_EP)),by="ID_AUUSAGES"],by="ID_AUUSAGES", all.x=T)
besoins_agreg2 = merge(besoins_agreg2 , besoins_bureautique[,list(BESOIN_BUREAUTIQUE = sum(BESOIN), BESOIN_BUREAUTIQUE_EP = sum(BESOIN_EP)),by="ID_AUUSAGES"],by="ID_AUUSAGES", all.x=T)
besoins_agreg2 = merge(besoins_agreg2 , besoins_eclairage[,list(BESOIN_ECLAIRAGE = sum(BESOIN), BESOIN_ECLAIRAGE_EP = sum(BESOIN_EP)),by="ID_AUUSAGES"],by="ID_AUUSAGES", all.x=T)
besoins_agreg2 = merge(besoins_agreg2 , besoins_froid[,list(BESOIN_FROID = sum(BESOIN), BESOIN_FROID_EP = sum(BESOIN_EP)),by="ID_AUUSAGES"],by="ID_AUUSAGES", all.x=T)
besoins_agreg2 = merge(besoins_agreg2 , besoins_autres[,list(BESOIN_AUTRES = sum(BESOIN), BESOIN_AUTRES_EP = sum(BESOIN_EP)),by="ID_AUUSAGES"],by="ID_AUUSAGES", all.x=T)


besoins_parc_init = merge(besoins_parc_init, besoins_agreg2[,list(CONSOU_ECS = CONSO_ECS/SURFACES,
                                                                  CONSOU_ECS_EP = CONSO_ECS_EP/SURFACES,
                                                                  BESOINU_CUISSON = BESOIN_CUISSON/SURFACES,
                                                                  BESOINU_CUISSON_EP = BESOIN_CUISSON_EP/SURFACES,
                                                                  BESOINU_ECLAIRAGE = BESOIN_ECLAIRAGE/SURFACES,
                                                                  BESOINU_ECLAIRAGE_EP = BESOIN_ECLAIRAGE_EP/SURFACES,
                                                                  BESOINU_PROCESS = BESOIN_PROCESS/SURFACES,
                                                                  BESOINU_PROCESS_EP = BESOIN_PROCESS_EP/SURFACES,
                                                                  BESOINU_BUREAUTIQUE = BESOIN_BUREAUTIQUE/SURFACES,
                                                                  BESOINU_BUREAUTIQUE_EP = BESOIN_BUREAUTIQUE_EP/SURFACES,
                                                                  BESOINU_FROID = BESOIN_FROID/SURFACES,
                                                                  BESOINU_FROID_EP = BESOIN_FROID_EP/SURFACES,
                                                                  BESOINU_AUTRES = BESOIN_AUTRES/SURFACES,
                                                                  BESOINU_AUTRES_EP = BESOIN_AUTRES_EP/SURFACES),
                                                            by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                                                                 "COD_OCCUPANT", "COD_PERIODE_DETAIL",
                                                                 "COD_PERIODE_SIMPLE")],
                          by=c("COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                               "COD_OCCUPANT", "COD_PERIODE_DETAIL",
                               "COD_PERIODE_SIMPLE"),all.x =T)

              
#### calc conso tous les usages #####
###chauffage
besoins_agreg2[,sum(BESOIN_CHAUFF)/10^9]
besoins_agreg2[,sum(CONSO_CHAUFF, na.rm=T)/10^9]

###clim
besoins_agreg2[,sum(BESOIN_CLIM)/10^9]
besoins_agreg2[,sum(CONSO_CLIM)/10^9]

###ecs
besoins_agreg2[,sum(BESOIN_ECS)/10^9]
besoins_agreg2[,sum(CONSO_ECS)/10^9]

###cuisson
besoins_agreg2[,sum(BESOIN_CUISSON, na.rm=T)/10^9]

###elec spe
besoins_agreg2[,sum(BESOIN_AUXILIAIRES)/10^9]
besoins_agreg2[,sum(BESOIN_VENTILATION)/10^9]
besoins_agreg2[,sum(BESOIN_PROCESS)/10^9]
besoins_agreg2[,sum(BESOIN_BUREAUTIQUE)/10^9]
besoins_agreg2[,sum(BESOIN_ECLAIRAGE)/10^9]
besoins_agreg2[,sum(BESOIN_FROID)/10^9]

besoins_agreg2[,sum(BESOIN_PROCESS,BESOIN_BUREAUTIQUE,BESOIN_ECLAIRAGE,BESOIN_FROID)/10^9]
besoins_agreg2[,sum(BESOIN_AUXILIAIRES,BESOIN_VENTILATION,BESOIN_PROCESS,BESOIN_BUREAUTIQUE,BESOIN_ECLAIRAGE,BESOIN_FROID)/10^9]
besoins_agreg2[,sum(BESOIN_AUXILIAIRES_CALC,BESOIN_VENTILATION,BESOIN_PROCESS,BESOIN_BUREAUTIQUE,BESOIN_ECLAIRAGE,BESOIN_FROID)/10^9]

###autres usages
besoins_agreg2[,sum(BESOIN_AUTRES)/10^9]


besoins_branche_EF = besoins_agreg2[,list(SURFACES = sum(SURFACES)/10^6,
                     CONSO_CHAUFF = sum(CONSO_CHAUFF, na.rm = T)/10^9,
                     BESOIN_AUXILIAIRES = sum(BESOIN_AUXILIAIRES_CALC)/10^9,
                     BESOIN_VENTILATION = sum(BESOIN_VENTILATION)/10^9, 
                     BESOIN_CLIM = sum(BESOIN_CLIM)/10^9, 
                     CONSO_CLIM = sum(CONSO_CLIM)/10^9,
                     BESOIN_ECS = sum(BESOIN_ECS)/10^9,
                     CONSO_ECS = sum(CONSO_ECS)/10^9,
                     BESOIN_CUISSON = sum(BESOIN_CUISSON, na.rm=T)/10^9,
                     BESOIN_PROCESS = sum(BESOIN_PROCESS)/10^9,
                     BESOIN_BUREAUTIQUE = sum(BESOIN_BUREAUTIQUE)/10^9,
                     BESOIN_ECLAIRAGE = sum(BESOIN_ECLAIRAGE)/10^9,
                     BESOIN_FROID = sum(BESOIN_FROID)/10^9,
                     BESOIN_ELEC_SPE = sum(BESOIN_AUXILIAIRES_CALC,
                                           BESOIN_VENTILATION,BESOIN_PROCESS,
                                           BESOIN_BUREAUTIQUE,BESOIN_ECLAIRAGE,
                                           BESOIN_FROID)/10^9,
                     BESOIN_AUTRES = sum(BESOIN_AUTRES)/10^9
                     ), by="BRANCHE"]

besoins_total_EF= besoins_agreg2[,list(BRANCHE = "Total EF",
                                       SURFACES = sum(SURFACES)/10^6,
                                       CONSO_CHAUFF = sum(CONSO_CHAUFF, na.rm = T)/10^9,
                                       BESOIN_AUXILIAIRES = sum(BESOIN_AUXILIAIRES_CALC)/10^9,
                                       BESOIN_VENTILATION = sum(BESOIN_VENTILATION)/10^9, 
                                       BESOIN_CLIM = sum(BESOIN_CLIM)/10^9, 
                                       CONSO_CLIM = sum(CONSO_CLIM)/10^9,
                                       BESOIN_ECS = sum(BESOIN_ECS)/10^9,
                                       CONSO_ECS = sum(CONSO_ECS)/10^9,
                                       BESOIN_CUISSON = sum(BESOIN_CUISSON, na.rm=T)/10^9,
                                       BESOIN_PROCESS = sum(BESOIN_PROCESS)/10^9,
                                       BESOIN_BUREAUTIQUE = sum(BESOIN_BUREAUTIQUE)/10^9,
                                       BESOIN_ECLAIRAGE = sum(BESOIN_ECLAIRAGE)/10^9,
                                       BESOIN_FROID = sum(BESOIN_FROID)/10^9,
                                       BESOIN_ELEC_SPE = sum(BESOIN_AUXILIAIRES_CALC,BESOIN_VENTILATION,
                                                             BESOIN_PROCESS,BESOIN_BUREAUTIQUE,BESOIN_ECLAIRAGE,BESOIN_FROID)/10^9,
                                       BESOIN_AUTRES = sum(BESOIN_AUTRES)/10^9)]

besoinsU_branche_EF=  besoins_branche_EF[,list(
  CONSO_CHAUFF =  CONSO_CHAUFF/SURFACES*10^3,
  BESOIN_AUXILIAIRES = BESOIN_AUXILIAIRES/SURFACES*10^3,
  BESOIN_VENTILATION =BESOIN_VENTILATION/SURFACES*10^3, 
  CONSO_CLIM = CONSO_CLIM/SURFACES*10^3,
  CONSO_ECS= CONSO_ECS/SURFACES*10^3,
  BESOIN_CUISSON =  BESOIN_CUISSON/SURFACES*10^3,
  BESOIN_PROCESS = BESOIN_PROCESS/SURFACES*10^3,
  BESOIN_BUREAUTIQUE = BESOIN_BUREAUTIQUE/SURFACES*10^3,
  BESOIN_ECLAIRAGE = BESOIN_ECLAIRAGE/SURFACES*10^3,
  BESOIN_FROID = BESOIN_FROID/SURFACES*10^3,
  BESOIN_ELEC_SPE = BESOIN_ELEC_SPE/SURFACES*10^3,
  BESOIN_AUTRES = BESOIN_AUTRES/SURFACES*10^3), by="BRANCHE"]

besoinsU_total_EF= besoins_total_EF[,list(BRANCHE = "Total EF",
                                          CONSO_CHAUFF =  CONSO_CHAUFF/SURFACES*10^3,
                                          BESOIN_AUXILIAIRES = BESOIN_AUXILIAIRES/SURFACES*10^3,
                                          BESOIN_VENTILATION =BESOIN_VENTILATION/SURFACES*10^3, 
                                          CONSO_CLIM= CONSO_CLIM/SURFACES*10^3,
                                          CONSO_ECS = CONSO_ECS/SURFACES*10^3,
                                          BESOIN_CUISSON =  BESOIN_CUISSON/SURFACES*10^3,
                                          BESOIN_PROCESS = BESOIN_PROCESS/SURFACES*10^3,
                                          BESOIN_BUREAUTIQUE = BESOIN_BUREAUTIQUE/SURFACES*10^3,
                                          BESOIN_ECLAIRAGE = BESOIN_ECLAIRAGE/SURFACES*10^3,
                                          BESOIN_FROID = BESOIN_FROID/SURFACES*10^3,
                                          BESOIN_ELEC_SPE = BESOIN_ELEC_SPE/SURFACES*10^3,
                                          BESOIN_AUTRES = BESOIN_AUTRES/SURFACES*10^3)]


besoins_branche_EP= besoins_agreg2[,list(
                                       SURFACES = sum(SURFACES)/10^6,
                                       CONSO_CHAUFF_EP = sum(CONSO_CHAUFF_EP, na.rm = T)/10^9,
                                       BESOIN_AUXILIAIRES_EP = sum(BESOIN_AUXILIAIRES_CALC_EP)/10^9,
                                       BESOIN_VENTILATION_EP = sum(BESOIN_VENTILATION_EP)/10^9, 
                                       CONSO_CLIM_EP = sum(CONSO_CLIM_EP)/10^9,
                                       CONSO_ECS_EP = sum(CONSO_ECS_EP)/10^9,
                                       BESOIN_CUISSON_EP = sum(BESOIN_CUISSON_EP, na.rm=T)/10^9,
                                       BESOIN_PROCESS_EP = sum(BESOIN_PROCESS_EP)/10^9,
                                       BESOIN_BUREAUTIQUE_EP = sum(BESOIN_BUREAUTIQUE_EP)/10^9,
                                       BESOIN_ECLAIRAGE_EP = sum(BESOIN_ECLAIRAGE_EP)/10^9,
                                       BESOIN_FROID_EP = sum(BESOIN_FROID_EP)/10^9,
                                       BESOIN_ELEC_SPE_EP = sum(BESOIN_AUXILIAIRES_CALC_EP,BESOIN_VENTILATION_EP,BESOIN_PROCESS_EP,
                                                                BESOIN_BUREAUTIQUE_EP,BESOIN_ECLAIRAGE_EP,BESOIN_FROID_EP,na.rm=T)/10^9,
                                       BESOIN_AUTRES_EP = sum(BESOIN_AUTRES_EP)/10^9), by="BRANCHE"]


besoins_total_EP= besoins_agreg2[,list(BRANCHE = "Total EP",
                                       SURFACES = sum(SURFACES)/10^6,
                                       CONSO_CHAUFF_EP = sum(CONSO_CHAUFF_EP, na.rm = T)/10^9,
                                       BESOIN_AUXILIAIRES_EP = sum(BESOIN_AUXILIAIRES_CALC_EP)/10^9,
                                       BESOIN_VENTILATION_EP = sum(BESOIN_VENTILATION_EP)/10^9, 
                                       CONSO_CLIM_EP = sum(CONSO_CLIM_EP)/10^9,
                                       CONSO_ECS_EP = sum(CONSO_ECS_EP)/10^9,
                                       BESOIN_CUISSON_EP = sum(BESOIN_CUISSON_EP, na.rm=T)/10^9,
                                       BESOIN_PROCESS_EP = sum(BESOIN_PROCESS_EP)/10^9,
                                       BESOIN_BUREAUTIQUE_EP = sum(BESOIN_BUREAUTIQUE_EP)/10^9,
                                       BESOIN_ECLAIRAGE_EP = sum(BESOIN_ECLAIRAGE_EP)/10^9,
                                       BESOIN_FROID_EP = sum(BESOIN_FROID_EP)/10^9,
                                       BESOIN_ELEC_SPE_EP = sum(BESOIN_AUXILIAIRES_CALC_EP,BESOIN_VENTILATION_EP,BESOIN_PROCESS_EP,
                                                                BESOIN_BUREAUTIQUE_EP,BESOIN_ECLAIRAGE_EP,BESOIN_FROID_EP,na.rm=T)/10^9,
                                       BESOIN_AUTRES_EP = sum(BESOIN_AUTRES_EP)/10^9)]

besoinsU_branche_EP=  besoins_branche_EP[,list(
                                          CONSO_CHAUFF_EP =  CONSO_CHAUFF_EP/SURFACES*10^3,
                                          BESOIN_AUXILIAIRES_EP = BESOIN_AUXILIAIRES_EP/SURFACES*10^3,
                                          BESOIN_VENTILATION_EP =BESOIN_VENTILATION_EP/SURFACES*10^3, 
                                          CONSO_CLIM_EP = CONSO_CLIM_EP/SURFACES*10^3,
                                          CONSO_ECS_EP = CONSO_ECS_EP/SURFACES*10^3,
                                          BESOIN_CUISSON_EP =  BESOIN_CUISSON_EP/SURFACES*10^3,
                                          BESOIN_PROCESS_EP = BESOIN_PROCESS_EP/SURFACES*10^3,
                                          BESOIN_BUREAUTIQUE_EP = BESOIN_BUREAUTIQUE_EP/SURFACES*10^3,
                                          BESOIN_ECLAIRAGE_EP = BESOIN_ECLAIRAGE_EP/SURFACES*10^3,
                                          BESOIN_FROID_EP = BESOIN_FROID_EP/SURFACES*10^3,
                                          BESOIN_ELEC_SPE_EP = BESOIN_ELEC_SPE_EP/SURFACES*10^3,
                                          BESOIN_AUTRES_EP = BESOIN_AUTRES_EP/SURFACES*10^3), by="BRANCHE"]

besoinsU_total_EP= besoins_total_EP[,list(BRANCHE = "Total EP",
                                       CONSO_CHAUFF_EP =  CONSO_CHAUFF_EP/SURFACES*10^3,
                                       BESOIN_AUXILIAIRES_EP = BESOIN_AUXILIAIRES_EP/SURFACES*10^3,
                                       BESOIN_VENTILATION_EP =BESOIN_VENTILATION_EP/SURFACES*10^3, 
                                       CONSO_CLIM_EP = CONSO_CLIM_EP/SURFACES*10^3,
                                       CONSO_ECS_EP = CONSO_ECS_EP/SURFACES*10^3,
                                       BESOIN_CUISSON_EP =  BESOIN_CUISSON_EP/SURFACES*10^3,
                                       BESOIN_PROCESS_EP = BESOIN_PROCESS_EP/SURFACES*10^3,
                                       BESOIN_BUREAUTIQUE_EP = BESOIN_BUREAUTIQUE_EP/SURFACES*10^3,
                                       BESOIN_ECLAIRAGE_EP = BESOIN_ECLAIRAGE_EP/SURFACES*10^3,
                                       BESOIN_FROID_EP = BESOIN_FROID_EP/SURFACES*10^3,
                                       BESOIN_ELEC_SPE_EP = BESOIN_ELEC_SPE_EP/SURFACES*10^3,
                                       BESOIN_AUTRES_EP = BESOIN_AUTRES_EP/SURFACES*10^3)]

##### part des 2tiquettes ?nergie ####

besoins_parc_init[, CONSO_USAGES_RT_EP := sum(CONSO_CHAUFF_EP, CONSOU_ECS_EP*SURFACES, CONSOU_CLIM_EP*SURFACES, 
                                           BESOIN_AUXILIAIRES_CALC*2.58, BESOIN_VENTILATION*2.58, 
                                           BESOINU_ECLAIRAGE_EP*SURFACES, na.rm=T), by="ID"]

besoins_parc_init[, CONSOU_USAGES_RT_EP := CONSO_USAGES_RT_EP/SURFACES,  by="ID"]
besoins_parc_init[, CONSOU_TOT_EP :=  sum(CONSO_CHAUFF_EP,CONSOU_ECS_EP*SURFACES, CONSOU_CLIM_EP*SURFACES, 
                                       BESOIN_AUXILIAIRES_CALC*2.58, BESOIN_VENTILATION*2.58, 
                                       BESOINU_ECLAIRAGE_EP*SURFACES,BESOINU_BUREAUTIQUE_EP*SURFACES,
                                       BESOINU_FROID_EP*SURFACES, BESOINU_PROCESS_EP*SURFACES,BESOINU_AUTRES_EP*SURFACES,
                                       BESOINU_CUISSON_EP*SURFACES, na.rm=T)/SURFACES, by="ID"]
besoins_parc_init[, CONSOU_TOT :=  sum(CONSO_CHAUFF,CONSOU_ECS*SURFACES, CONSOU_CLIM*SURFACES, 
                                          BESOIN_AUXILIAIRES_CALC, BESOIN_VENTILATION, 
                                          BESOINU_ECLAIRAGE*SURFACES,BESOINU_BUREAUTIQUE*SURFACES,
                                          BESOINU_FROID*SURFACES, BESOINU_PROCESS*SURFACES,BESOINU_AUTRES*SURFACES,
                                          BESOINU_CUISSON*SURFACES, na.rm=T)/SURFACES, by="ID"]

Bornes_etiq = fread("table_param_origine/Etiquettes_Bornes.csv",  
                    colClasses = list("character" = c("CATEGORIE_ETIQUETTE","ETIQUETTE")))
Typebatiment_etiq = fread("table_param_origine/Etiquettes_Categories.csv",  
                          colClasses = list("character" = c("CATEGORIE_ETIQUETTE","BRANCHE", "BAT_TYPE")))



quantile(besoins_parc_init$CONSOU_USAGES_RT_EP)

etiq_ini = merge(besoins_parc_init, 
                 Typebatiment_etiq[,list(COD_BRANCHE = BRANCHE,
                                         COD_BAT_TYPE = BAT_TYPE, CATEGORIE_ETIQUETTE)], 
                 by=c("COD_BRANCHE","COD_BAT_TYPE"),all.x=T )


check_etiq = function(conso_U,categorie_bat){
  etiq = Bornes_etiq[CATEGORIE_ETIQUETTE == categorie_bat & 
                       conso_U >= CONSO_MIN & conso_U < CONSO_MAX,  ETIQUETTE]
  return(etiq)
}

etiq_ini[,ETIQUETTE := check_etiq(CONSOU_USAGES_RT_EP, CATEGORIE_ETIQUETTE),by="ID"]

etiq_ini[,sum(SURFACES, na.rm=T)/10^6/surf_tot_tertiaire, by="ETIQUETTE"][order(ETIQUETTE)]



