#source("analyse_param_utilisateurs.R")

require(data.table)
require(ggplot2)

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
# 

##### GRAPH PARC ENTRANT #######

# ##### graph conso batiments entrants
# 
# summary(conso_entrants_usage$conso_RT)
# ggplot() + geom_bar(data = conso_entrants_usage[BRANCHE == "Bureaux Administration"],
#                     aes(y = conso_RT, x = BAT_TYPE, fill = BAT_TYPE), stat = "identity" ) + facet_grid(~période) +
#   mytheme_plot 
# 
# branchelist = unique(conso_entrants$BRANCHE)
# 
# for(branche_tmp in branchelist){
#   
#   ggplot(data = conso_entrants[BRANCHE == branche_tmp],
#          aes(y = conso_unitaire, x = période, fill = USAGE)) + 
#     geom_bar(stat = "identity" ) + 
#     facet_wrap(~BAT_TYPE, labeller = labeller(BAT_TYPE = label_wrap_gen(10))) +
#     mytheme_facet_plot+ theme(axis.text = element_text(angle = 90)) +
#     scale_fill_discrete("Usage", breaks = rev(levels(conso_entrants$USAGE))) 
#   
#   ggsave(filename=paste0("figures/Conso_entrant/conso_tot",branche_tmp,".png"), width = 20, height = 16)
#   
#   ggplot(data = conso_entrants[BRANCHE == branche_tmp & USAGE %in% c("Chauffage","Climatisation","Auxiliaires","Ventilation","ECS",
#                                                                      "Eclairage"),],
#          aes(y = conso_unitaire, x = période, fill = USAGE)) + 
#     geom_bar(stat = "identity" ) + 
#     facet_wrap(~BAT_TYPE, labeller = labeller(BAT_TYPE = label_wrap_gen(10))) +
#     mytheme_facet_plot+ theme(axis.text = element_text(angle = 90)) +
#     scale_fill_discrete("Usage", breaks = rev( c("Chauffage","Climatisation","Auxiliaires","Ventilation","ECS",
#                                                  "Eclairage")))
#   ggsave(filename=paste0("figures/Conso_entrant/conso_RT",branche_tmp,".png"), width = 20, height = 16)
# }
# 
# 
# for(branche_tmp in branchelist){
#   ggplot(data = conso_entrants[BRANCHE == branche_tmp & période %in% c("Etat de référence RT2000", 
#                                                                        "2040-2050")],
#          aes(y = conso_unitaire, x = période, fill = USAGE)) + 
#     geom_bar(stat = "identity" ) + 
#     facet_wrap(~BAT_TYPE, labeller = labeller(BAT_TYPE = label_wrap_gen(10))) +
#     mytheme_facet_plot+ theme(axis.text = element_text(angle = 90)) + 
#     scale_fill_discrete("Usage", breaks = rev(levels(conso_entrants$USAGE)))
#   ggsave(filename=paste0("figures/Conso_entrant/conso_tot_ref",branche_tmp,".png"), width = 20, height = 16)
#   
#   ggplot(data = conso_entrants[BRANCHE == branche_tmp & USAGE %in% c("Chauffage","Climatisation",
#                                                                      "Auxiliaires","Ventilation",
#                                                                      "ECS",
#                                                                      "Eclairage") & période %in% 
#                                  c("Etat de référence RT2000", "2040-2050"),],
#          aes(y = conso_unitaire, x = période, fill = USAGE)) + 
#     geom_bar(stat = "identity" ) + 
#     facet_wrap(~BAT_TYPE, labeller = labeller(BAT_TYPE = label_wrap_gen(10))) +
#     mytheme_facet_plot+ theme(axis.text = element_text(angle = 90)) + 
#     scale_fill_discrete("Usage", breaks = rev( c("Chauffage","Climatisation",
#                                                  "Auxiliaires","Ventilation","ECS",
#                                                  "Eclairage")))
#   ggsave(filename=paste0("figures/Conso_entrant/conso_RT_ref",branche_tmp,".png"), width = 20, height = 16)
#   
# }

##### GRAPH PARC EXISTANT #######

####### PM des différentes énergies par branche

graph_PM_ENERGIE_BRANCHE = ggplot(data = PM_ENERGIE_BRANCHE,
       aes(y =  SurfaceMm2, x = BRANCHE, fill = ENERGIE)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Surface en Mm2 en 2009") + xlab("Branche") + 
  scale_fill_discrete("Energie de \nChauffage", breaks = rev( c("Electricité","Gaz","Fioul","Urbain","Autres")))

#ggsave(filename=paste0("figures/PM_existant/SURF_ENERGIE_CHAUFF_BRANCHE.png"), width = 20, height = 16)


graph_PM_ENERGIE_BRANCHE2 = ggplot(data = PM_ENERGIE_BRANCHE,
       aes(y = PM_Energie, x = BRANCHE, fill = ENERGIE)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Part du parc en 2009") + xlab("Branche") + 
  geom_text(aes(y = PM_Energie_cum, x = BRANCHE,label = paste0(signif(PM_Energie*100, digits=2), " %")), 
            color = "white", vjust = 1.1, size = 10)+
  scale_fill_discrete("Energie de \nChauffage", breaks = rev( c("Electricité","Gaz","Fioul","Urbain","Autres")))

#ggsave(filename=paste0("figures/PM_existant/PM_ENERGIE_CHAUFF_BRANCHE.png"), width = 20, height = 16)

####### PM des différentes énergies par période

ggplot(data = PM_ENERGIE_PERIODE_SIMPLE,
       aes(y =  SurfaceMm2, x = PERIODE_SIMPLE, fill = ENERGIE)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Surface en Mm2 en 2009") + xlab("PERIODE_SIMPLE") + 
  scale_fill_discrete("Energie de \nChauffage", breaks = rev( c("Electricité","Gaz","Fioul","Urbain","Autres")))

#ggsave(filename=paste0("figures/PM_existant/SURF_ENERGIE_CHAUFF_PERIODE_SIMPLE.png"), width = 20, height = 16)



ggplot(data = PM_ENERGIE_PERIODE_SIMPLE,
       aes(y = PM_Energie, x = PERIODE_SIMPLE, fill = ENERGIE)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Part du parc en 2009") + xlab("PERIODE_SIMPLE") + 
  geom_text(aes(y = PM_Energie_cum, x = PERIODE_SIMPLE,label = paste0(signif(PM_Energie*100, digits=2), " %")), 
            color = "white", vjust = 1.1, size = 10)+
  scale_fill_discrete("Energie de \nChauffage", breaks = rev( c("Electricité","Gaz","Fioul","Urbain","Autres")))

#ggsave(filename=paste0("figures/PM_existant/PM_ENERGIE_CHAUFF_PERIODE_SIMPLE.png"), width = 20, height = 16)

##### PM des systèmes de chauffages 


graph_PM_ENERGIE_SYSTEME_CHAUD = ggplot(data = PM_ENERGIE_SYSTEME_CHAUD,
       aes(y =  SurfaceMm2, x = SYSTEME_CHAUD, fill = ENERGIE)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Surface en Mm2 en 2009") + xlab("SYSTEME_CHAUD") + 
  scale_fill_discrete("Energie de \nChauffage", breaks = rev( c("Electricité","Gaz","Fioul","Urbain","Autres")))

#ggsave(filename=paste0("figures/PM_existant/SURF_ENERGIE_CHAUFF_SYSTEME_CHAUD.png"), width = 20, height = 16)


ggplot(data = PM_ENERGIE_SYSTEME_CHAUD,
       aes(y = PM_Energie, x = SYSTEME_CHAUD, fill = ENERGIE)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Part du parc en 2009") + xlab("SYSTEME_CHAUD") + 
  geom_text(aes(y = PM_Energie_cum, x = SYSTEME_CHAUD,label = paste0(signif(PM_Energie*100, digits=2), " %")), 
            color = "white", vjust = 1.1, size = 10)+
  scale_fill_discrete("Energie de \nChauffage", breaks = rev( c("Electricité","Gaz","Fioul","Urbain","Autres")))

#ggsave(filename=paste0("figures/PM_existant/PM_ENERGIE_CHAUFF_SYSTEME_CHAUD.png"), width = 20, height = 16)


ggplot(data = PM_ENERGIE_SYSTEME_CHAUD2,
       aes(y = PM_SYSTEME_CHAUD, x = ENERGIE, fill = SYSTEME_CHAUD)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Part du parc en 2009") + xlab("Energie de chauffage") + 
  geom_text(data = PM_ENERGIE_SYSTEME_CHAUD[PM_SYSTEME_CHAUD >0.04,],aes(y = PM_SYSTEME_CHAUD_cum, x = ENERGIE,label = 
                                                                           paste0(signif(PM_SYSTEME_CHAUD*100, digits=2), " %")), 
            color = "white", vjust = 1.1, size = 10) +
  scale_fill_discrete("Sytème de chauffage", breaks = rev( c("Chaudière gaz",
                                                             "Chaudière fioul", 
                                                             "Electrique direct", "PAC",
                                                             "Rooftop", "Tube radiant", "Cassette rayonnante",
                                                             "DRV", "Autre système centralisé", 
                                                             "nr")))

#ggsave(filename=paste0("figures/PM_existant/PM_SYSTEME_CHAUD_par_ENERGIE.png"), width = 20, height = 16)

##### graph PM climatisé 

graph_PM_CLIM_BRANCHE = ggplot(data = PM_CLIM_BRANCHE,
                                   aes(y = PM_CLIM, x = BRANCHE, fill =  EQ_CLIM)) + 
  geom_bar(stat = "identity" ) + 
  mytheme_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Part du parc en 2009") + xlab("Branche") + 
  geom_text(aes(y = PM_CLIM_cum, x = BRANCHE,label = paste0(signif(PM_CLIM*100, digits=2), " %")), 
            color = "white", vjust = 1.1, size = 10)+
  scale_fill_discrete("")

setkeyv(PM_CLIM_SS_BRANCHE,c("BRANCHE","SS_BRANCHE", "EQ_CLIM"))
ordre = unique(PM_CLIM_SS_BRANCHE$SS_BRANCHE)
PM_CLIM_SS_BRANCHE[, SS_BRANCHE:=factor(SS_BRANCHE, levels = ordre )]

graph_PM_CLIM_SS_BRANCHE = ggplot(data = PM_CLIM_SS_BRANCHE,
                               aes(y = PM_CLIM, x = SS_BRANCHE , fill =  EQ_CLIM))+
  geom_bar(stat = "identity" ) + 
  mytheme_facet_plot + theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1), legend.position = "right", 
                       strip.background = element_rect(fill = "white",color= "black"), 
                       panel.background = element_rect(fill = "white",color= "black")) + 
  ylab("Part du parc en 2009") + xlab("Sous-Branche") +
  scale_fill_discrete("")
graph_PM_CLIM_SS_BRANCHE


##### variabilité des coûts des gestes
cout_geste[COUT >0]

var_cout_pargeste = ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008"),]) + 
  geom_boxplot(aes(GESTE, COUT))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

var_cout_parSS_BRANCHE = ggplot(cout_geste[COUT >0 & GESTE != "GTB" & PERIODE_SIMPLE  %in% c("Av 1980","1981-1998","1999-2008"),]) + 
  geom_point(aes(SS_BRANCHE, COUT, color = BRANCHE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 18, hjust = 1))

var_cout_parSS_BRANCHE + facet_wrap(~GESTE, nrow = 4)


var_cout_parSS_BRANCHE_ENS_BBC = ggplot(cout_geste[COUT >0 &PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008") & GESTE == "ENSBBC",]) + 
  geom_point(aes(SS_BRANCHE, COUT, color = BRANCHE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))


ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008","2009-2015","2041-2050") & GESTE == "ENSBBC",]) + 
  geom_point(aes(SS_BRANCHE, COUT, color = PERIODE_SIMPLE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))


summary(lm(COUT~ BRANCHE  + GESTE + PERIODE_SIMPLE , data = cout_geste))
summary(lm(COUT~ GESTE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(COUT~ GESTE + BAT_TYPE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(COUT~ GESTE + BAT_TYPE + PERIODE_SIMPLE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(COUT~ BRANCHE  + GESTE + PERIODE_SIMPLE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(COUT~ BAT_TYPE  + GESTE + PERIODE_SIMPLE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(COUT~ BAT_TYPE  + GESTE , data = cout_geste[PERIODE_SIMPLE %in% c("Av 1980"),]))

##### graphs variabilité des gains associés 

var_gain_pargeste = ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008"),]) + 
  geom_boxplot(aes(GESTE, GAIN))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))


var_gain_parSS_BRANCHE = ggplot(cout_geste[COUT >0 & GESTE != "GTB" & PERIODE_SIMPLE  %in% c("Av 1980","1981-1998","1999-2008"),]) + 
  geom_point(aes(SS_BRANCHE, GAIN, color = BRANCHE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 18, hjust = 1))

var_gain_parSS_BRANCHE  + facet_wrap(~GESTE, nrow = 4)

var_gain_parPERIODE = ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008"),]) + 
  geom_boxplot(aes(GESTE, GAIN, color=PERIODE_SIMPLE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot + scale_color_discrete("Période de \n construction") +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))


var_gain_parPERIODE2 = ggplot(cout_geste[COUT >0,]) + 
  geom_boxplot(aes(GESTE, GAIN, color=PERIODE_SIMPLE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot + scale_color_discrete("Période de \n construction") +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

summary(lm(GAIN~ GESTE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(GAIN~ GESTE + BAT_TYPE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(GAIN~ GESTE + BAT_TYPE + PERIODE_SIMPLE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))


summary(lm(GAIN~ BRANCHE  + GESTE + PERIODE_SIMPLE, data = cout_geste[PERIODE_SIMPLE %in%  c("Av 1980","1981-1998","1999-2008"),]))

##### graphs coût et gains associés par branche, périodes avant 2015

summary(lm(COUT ~ GAIN, 
           data= cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(COUT ~ GAIN + BRANCHE:PERIODE_SIMPLE, 
           data= cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008"),]))
summary(lm(COUT ~  GAIN:BRANCHE + BRANCHE:PERIODE_SIMPLE, 
           data= cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008"),]))

nuage_cout_gain_av1980 = ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980")]) + 
  geom_point(aes(GAIN, COUT, color= GESTE)) + geom_smooth(aes(GAIN, COUT)) + 
  mytheme_plot

nuage_cout_gain_av1980_sansresto = ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980") & COD_BRANCHE != "02"]) + 
  geom_point(aes(GAIN, COUT, color= GESTE)) + geom_smooth(aes(GAIN, COUT)) + facet_wrap(~ BRANCHE) + mytheme_facet_plot

nuage_cout_gain_av1980_sansresto = ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980") & COD_BRANCHE != "02"]) + 
  geom_point(aes(GAIN, COUT, color= GESTE)) + geom_smooth(aes(GAIN, COUT, color = SS_BRANCHE)) + 
                                                            facet_wrap(~ BRANCHE) + mytheme_facet_plot


ggplot(cout_geste[COUT >0 & PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008") & COD_SS_BRANCHE == "01"]) + geom_point(aes(GAIN, COUT)) + geom_smooth(aes(GAIN, COUT)) + 
  mytheme_plot

#ggsave(filename="figures/Couts_systemes/Cout_geste_par_branche.png", width = 20, height = 16)


##### graphs coût et CEE accordés par branche, périodes avant 2015

ggplot(cout_geste[PERIODE_SIMPLE %in% c("Av 1980","1981-1998","1999-2008","2009-2015"),]) + 
  geom_point(aes(GAIN, CEE, color = GESTE))  + theme(text = element_text(size = 20)) +
  facet_wrap(~BRANCHE ,labeller = 
               labeller(BRANCHE =label_wrap_gen(20))) + geom_smooth() + 
  mytheme_facet_plot 


##### GRAPH COUTS CHAUFFAGES #######


#### variabilité des coûts des systèmes

var_cout_parsyst = ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0 ,]) + 
  geom_boxplot(aes(PRODUCTION_CHAUD, COUT))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

var_coutsyst_parSS_BRANCHE = ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0 & PRODUCTION_CHAUD %in% nom_syst_non_performant,]) + 
  geom_point(aes(SS_BRANCHE, COUT, color = BRANCHE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 18, hjust = 1))

var_coutsyst_parSS_BRANCHE = ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0 & PRODUCTION_CHAUD %in% nom_syst_non_performant & 
                                                      PRODUCTION_CHAUD %in% c("Chaudière Gaz","Chaudière fioul","PAC", 
                                                                              "Rooftop",
                                                                              "Autre système centralisé"),]) + 
  geom_point(aes(SS_BRANCHE, COUT, color = BRANCHE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 18, hjust = 1))

var_coutsyst_parSS_BRANCHE + facet_wrap(~PRODUCTION_CHAUD, nrow = 4)

##### variabilité des rendements 

var_rdt_parsyst = ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0 ,]) + 
  geom_boxplot(aes(PRODUCTION_CHAUD, RDT))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

##### graphs coût et rdt des sytèmes


nuage_rdt_cout_syst = ggplot(cout_rdt_chauff[PERIODE == "0" & PRODUCTION_CHAUD %in% nom_syst_non_performant]) + 
  geom_point(aes(RDT, COUT, color= PRODUCTION_CHAUD)) + 
  mytheme_plot

##### graphs coût par branche et les systèmes performants, période de référence

ggplot(cout_rdt_chauff[PERIODE == "0" & PRODUCTION_CHAUD %in% nom_syst_non_performant,]) + 
  geom_point(aes(RDT, COUT, color = PRODUCTION_CHAUD))  + facet_wrap(~BRANCHE) + mytheme_facet_plot 

#ggsave(filename="figures/Couts_systemes/Cout_syst_chauff_perf_par branche_perioderef.png", width = 20, height = 16)

##### GRAPH COUTS CLIMATISATION #######

### variabilité par ss branche des coûts des systèmes de climatisation
var_cout_clim = 
ggplot(cout_rdt_clim[PERIODE == "0" & COUT >0 ,]) + 
  geom_point(aes(SS_BRANCHE, COUT, color= BRANCHE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

##### variabilité par ss branche des rdts des systèmes de climatisation
var_rdt_clim = 
  ggplot(cout_rdt_clim[PERIODE == "0" & COUT >0 ,]) + 
  geom_point(aes(SS_BRANCHE, RDT, color= BRANCHE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

#ggsave(filename="figures/Couts_systemes/Cout_syst_clim_par_branche_perioderef.png", width = 20, height = 16)



##### graphs évolution des coûts par branche 
evol_cout_clim = 
  ggplot(cout_rdt_clim[ COUT >0 ,]) + 
  geom_point(aes(PERIODE_LONG, COUT))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot + 
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

evol_rdt_clim = 
  ggplot(cout_rdt_clim[ COUT >0 ,]) + 
  geom_point(aes(PERIODE_LONG, RDT))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot + 
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))


#### calc coûts annuels d'investissement
cout_rdt_clim[,ID_AGREG := NULL]

col_com = names(cout_rdt_clim)[names(cout_rdt_clim)%in% names(parc_init)]

names(parc_init)[names(parc_init)%in% names(cout_rdt_clim) == F]

parc_aggreg = parc_init[, list(SURFACES = sum(SURFACES)), by= col_com]

col_com = col_com[col_com %in% c("EQ_CLIM", "COD_EQ_CLIM") == F]


sum(parc_aggreg$SURFACES/10^6)


calc_clim = merge(parc_aggreg , 
                  cout_rdt_clim[PERIODE == "0" & COD_EQ_CLIM == "01",c(col_com,"COUT","RDT"), with=F ], by=col_com, all.x = T)

sum(calc_clim$SURFACES/10^6)


Inv_clim_2010 = calc_clim[, list(Surface_tot = sum(SURFACES)/10^6, 
                                 coutm2 = mean(COUT),
                                 surface_clim = sum(SURFACES*0.01)/10^6, 
                                 cout_clim = sum(SURFACES*0.01*COUT)/10^6), by=c("BRANCHE","SS_BRANCHE","BAT_TYPE")]


Inv_clim_2010_branche = calc_clim[, list(Surface_tot = sum(SURFACES)/10^6, 
                                 coutm2_moyen = mean(COUT),
                                 surface_clim = sum(SURFACES*0.01)/10^6, 
                                 cout_clim = sum(SURFACES*0.01*COUT)/10^6), by="BRANCHE"]

Inv_clim_2010[,sum(cout_clim)]


#### deuxième calcul 


Surf_clim = parc_aggreg[COD_EQ_CLIM == "01", list(Surface_clim = sum(SURFACES)/10^6), by=c("BRANCHE","SS_BRANCHE","BAT_TYPE")]

Surf_tot = parc_aggreg[, list(Surface_tot = sum(SURFACES)/10^6), by=c("BRANCHE","SS_BRANCHE","BAT_TYPE")]

Inv_clim_2010 = merge(Surf_clim,Surf_tot,  by=c("BRANCHE","SS_BRANCHE","BAT_TYPE"))
Inv_clim_2010[, Part_clim_2009 := Surface_clim/Surface_tot]

Inv_clim_2010 = merge(Inv_clim_2010 , 
                      cout_rdt_clim[PERIODE == "0" & COD_EQ_CLIM == "01",c(col_com,"COUT","RDT"), with=F ], by=c("BRANCHE","SS_BRANCHE","BAT_TYPE"), all.x = T)


Inv_clim_2010[Part_clim_2009 <1 , Part_clim_2010 := Part_clim_2009 + 0.01]
Inv_clim_2010[Part_clim_2009 ==1 , Part_clim_2010 := Part_clim_2009]
Inv_clim_2010[, surface_clim_add_2010  := (Part_clim_2010*Surface_tot- Surface_clim)]
Inv_clim_2010[, cout_clim  := surface_clim_add_2010*COUT]
Inv_clim_2010[,sum(cout_clim)]

######  distribution des conso unitaires
besoins_agreg2[,]
ggplot(besoins_agreg2) + geom_boxplot(aes(BRANCHE ,CONSO_CHAUFF_EP/SURFACES)) + mytheme_facet_plot
ggplot(besoins_agreg2) + geom_boxplot(aes(BRANCHE ,CONSOU_USAGES_RT_EP)) + mytheme_facet_plot
ggplot(besoins_agreg2) + geom_boxplot(aes(BRANCHE , CONSOU_TOT)) + mytheme_facet_plot

