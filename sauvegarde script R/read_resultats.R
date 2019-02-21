require(rJava)
require(xlsx)
require(data.table)

Consoannee = read.xlsx(file = "table_resultat/table_resultat_sansmasque.xls", 
                       sheetName= "consoAnnee",header = T, colIndex = 1:8, endRow = 16000)

Consoannee = read.xlsx(file = "table_resultat/table_resultat_sansmasque.xls", 
                       sheetName= "consoAnnee",header = T, colIndex = 1:8, endRow = 1000)


#Consoannee = read.xlsx(file = "table_resultat/table_resultat_sansmasque.xls", sheetName= "consoAnnee",
#                       endRow = 500, header = T)

ncol(Consoannee)
example(read.xls)
findPerl


Consoannee = data.table()
