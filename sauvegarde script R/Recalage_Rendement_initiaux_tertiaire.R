require(data.table)
require(ggplot2)

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

##### besoins init chauffage
besoins_chauffage_init = fread("../tertiaire/parametrage modèle/Besoin_chauffage_init.csv", dec = ",", 
                               colClasses =
                                 list("character"=c("ID_segment","ID_AGREG2", "ID_AGREG", "ID_AGREG_RDT_COUT", 
                                                    "ID","ID_BRANCHE","ID_BAT_TYPE","ID_PRODUCTION_CHAUD","ID_ENERGIE"),
                                      "numeric"= c("RDT systeme")))

besoins_chauffage_init[,ID_AGREG := paste0(substring(ID, 1,6), substring(ID, 9,12))] 
besoins_chauffage_init[,ID_AGREG_calibCINT := paste0(substring(ID, 1,8), substring(ID, 9,10))] 
summary(besoins_chauffage_init )


besoins_chauffage_init[,COD_BRANCHE := substring(ID_AGREG, 1,2)]
besoins_chauffage_init[,COD_SS_BRANCHE := substring(ID_AGREG, 3,4)]
besoins_chauffage_init[,COD_BAT_TYPE := substring(ID_AGREG, 5,6)]
besoins_chauffage_init[,COD_PERIODE_DETAIL := substring(ID_AGREG, 7,8)] 
besoins_chauffage_init[,COD_PERIODE_SIMPLE := substring(ID_AGREG, 9,10)]
besoins_chauffage_init[,COD_SYSTEME_CHAUD := substring(ID_AGREG, 9,10)]
besoins_chauffage_init[, COD_SYSTEME_CHAUD:=substring(ID, 13,14)]
besoins_chauffage_init[, COD_ENERGIE :=substring(ID, 17,18)]

#### noms systèmes 
besoins_chauffage_init =merge(besoins_chauffage_init , COD_BRANCHE, by="COD_BRANCHE")

besoins_chauffage_init =merge(besoins_chauffage_init , COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD")
besoins_chauffage_init =merge(besoins_chauffage_init , COD_ENERGIE, by="COD_ENERGIE")


colors_syst= 
  data.table(SYSTEME_CHAUD =factor(unique(COD_SYSTEME_CHAUD$SYSTEME_CHAUD), 
                                   levels = c("Chaudière gaz","Chaudière condensation gaz",
                                              "Tube radiant", "Tube radiant performant" ,
                                              "Chaudière fioul","Chaudière condensation fioul",
                                              "Electrique direct","Electrique direct performant",
                                              "Cassette rayonnante","Cassette rayonnante performant",
                                              "PAC","PAC performant",
                                              "Rooftop", "Rooftop performant",
                                              "DRV", "DRV performant",
                                              "Autre système centralisé",  "Autre système centralisé performant",
                                              "nr","NA"))
  )

colors_syst = colors_syst[order(SYSTEME_CHAUD)]
colors_syst[,color := c("dodgerblue1","dodgerblue4", "blue1","blue4","springgreen1","springgreen4",
                        "yellow1","yellow4","gold1","gold4","chocolate1","chocolate4","coral1","coral3",
                        "salmon1","salmon4","red1","red4","gray86","gray86")]


####part des systèmes initialement par branche

Part_syst = besoins_chauffage_init[,list(surf = sum(`SURFACES 2009`)), by=c("BRANCHE","Système chaud")]
Part_syst[, PM := surf/(sum(surf)), by="BRANCHE"]

ggplot(Part_syst) + geom_bar(aes(BRANCHE,PM,fill = get("Système chaud")), stat = "identity")


#### rendements initiaux  parc
rdt_syst = besoins_chauffage_init[,list(surf = sum(`SURFACES 2009`), rdt = sum(BESOIN)/sum(BESOIN/`RDT systeme`)), by=c("BRANCHE","Système chaud", "ENERGIE")]

ggplot(rdt_syst) + geom_bar(aes(get("BRANCHE"),rdt,fill = get("BRANCHE")), stat = "identity") + facet_wrap(~`Système chaud` + ENERGIE)

rdt_syst_natio = besoins_chauffage_init[,list(surf = sum(`SURFACES 2009`), rdt = sum(BESOIN)/sum(BESOIN/`RDT systeme`)), by=c("Système chaud", "ENERGIE")]

0.72*1.30

(0.72*0.75+0.9*0.25)


### rendements BDD modèle

require(readxl)

bibli_model = read_excel("../tertiaire/parametrage modèle/couts_rdt_syst/parametrage_cout_syst_chauffage_CODAH.xlsx",sheet = "bibli_syst_modif")

bibli_model = data.table(bibli_model)


rdt_syst2 = bibli_model [,list( 
                            RDT_ini = mean(RDT), 
                            RDT_modif = mean(RDT_modif), 
                            COUT = mean(COUT), 
                            COUT_modif = mean(COUT_modif)), by=c("BRANCHE","PRODUCTION_CHAUD", "ENERGIE")]

rdt_syst2 = merge(rdt_syst2,rdt_syst[,list(BRANCHE, PRODUCTION_CHAUD = `Système chaud`, ENERGIE,  rdt)], by=c("BRANCHE","PRODUCTION_CHAUD", "ENERGIE"), all.x=T)

rdt_syst2[1:50]
rdt_syst2[51:103]

rdt_syst2[PRODUCTION_CHAUD %in% c("Chaudière gaz","Chaudière condensation gaz")]
rdt_syst2[PRODUCTION_CHAUD %in% c("Chaudière gaz","Chaudière condensation gaz"), COUT_modif/COUT]
rdt_syst2[ENERGIE %in% c("Gaz")]
rdt_syst2[ENERGIE %in% c("Electricité"), ]
rdt_syst2[ENERGIE %in% c("Autres"), ]
rdt_syst2[ENERGIE %in% c("Urbain"), ]
rdt_syst2[ENERGIE %in% c("Fioul"), ]

##### gain liés au remplacement par une chaudière neuve identique
rdt_syst2[, gain_renouv := RDT_modif/rdt-1]

rdt_syst2[ENERGIE %in% c("Gaz"), ]

##### gain liés à la chaudière condensation
techno_comp = dcast.data.table(rdt_syst2, BRANCHE + ENERGIE ~ PRODUCTION_CHAUD, value.var = "RDT_ini")

techno_comp[ENERGIE %in% c("Gaz"), gain_perf := get("Chaudière condensation gaz")/get("Chaudière gaz") -1 ]


techno_comp[ENERGIE %in% c("Gaz"), ]

techno_comp[ENERGIE %in% c("Fioul"), gain_perf := get("Chaudière condensation fioul")/get("Chaudière fioul") -1 ]


techno_comp[ENERGIE %in% c("Fioul"), ]

##### gain liés à la PACelec par rapport à élec joule
techno_comp[ENERGIE %in% c("Electricité"), gain_perf := get("PAC")/get("Electrique direct") -1 ]

techno_comp[ENERGIE %in% c("Electricité"), ]

##### gain liés à la PACperf par rapport à PAC
techno_comp[ENERGIE %in% c("Electricité"), gain_perf := get("PAC performant")/get("PAC") -1 ]

techno_comp[ENERGIE %in% c("Electricité"), ]

##### gain liés à la PACperf par rapport à PAC


##### Surcoût liés à la chaudière condensation
techno_comp_cout = dcast.data.table(rdt_syst2, BRANCHE + ENERGIE ~ PRODUCTION_CHAUD, value.var = "COUT")
techno_comp_cout [ENERGIE %in% c("Gaz"), surcout_perf := get("Chaudière condensation gaz")/get("Chaudière gaz") -1 ]
techno_comp_cout[ENERGIE %in% c("Gaz"), ]

##### Surcoût liés à la PAC élec

techno_comp_cout[ENERGIE %in% c("Electricité"), surcout_perf := get("PAC")/get("Electrique direct") -1 ]
techno_comp_cout[ENERGIE %in% c("Electricité"), ]

##### Surcoût liés à la chaudière fioul

techno_comp_cout [ENERGIE %in% c("Fioul"), surcout_perf := get("Chaudière condensation fioul")/get("Chaudière fioul") -1 ]
techno_comp_cout[ENERGIE %in% c("Fioul"), ]


### coût moyen
bibli_model[PERIODE=="1", list(mean(COUT),mean(RDT)), by=c("PRODUCTION_CHAUD", "ENERGIE")]
bibli_model[BRANCHE != "Transport", list(COUT=mean(COUT),COUT=mean(COUT_modif),RDT= mean(RDT_modif)), by=c("PRODUCTION_CHAUD")][order(PRODUCTION_CHAUD)]
bibli_model[PRODUCTION_CHAUD == "Electrique direct", RDT_modif]

bibli_model[PERIODE=="1", list(COUT=mean(COUT),RDT= mean(RDT)), by=c("BRANCHE","PRODUCTION_CHAUD")]
write.table(bibli_model[, list(COUT=mean(COUT),RDT= mean(RDT)), by=c("BRANCHE","PRODUCTION_CHAUD")],
            "../tertiaire/parametrage modèle/couts_rdt_syst/Rdtcoutmoyensystparbranche.csv", quote = T, row.names = F, sep=";")
write.table(bibli_model[PERIODE=="1",, list(COUT=mean(COUT),RDT= mean(RDT)), by=c("PRODUCTION_CHAUD", "ENERGIE")],
            "../tertiaire/parametrage modèle/couts_rdt_syst/Rdtcoutmoyensyst.csv", quote = T, row.names = F, sep=";")
