import numpy as np
from numpy import genfromtxt
from numpy import array
from sklearn.metrics import accuracy_score, f1_score, precision_score, recall_score, classification_report, confusion_matrix


# In[2]:

#read dataset into array
dataset= genfromtxt('finalresult.csv',skip_header =2, delimiter=',')
#check the size of dataset
print(np.shape(dataset))

#remove first row that contains columnnames
true_label=genfromtxt('finalresult.csv',skip_header =2, delimiter=',',dtype=str,usecols=(6))
print(array(true_label))
#np.reshape(true_label,(81,1))
print(np.shape(true_label))
#remove first 2 columns (Name and MD5) as they will not have any contribution
predicted=genfromtxt('finalresult.csv',skip_header =2, delimiter=',',dtype=str,usecols=(7))
print(predicted)

con_mat=confusion_matrix(true_label, predicted, labels=["walk", "stand"])
print("******************************")
print("The confusion_matrix is :")
print(con_mat)
print("accuracy_score :",accuracy_score(true_label, predicted))
print("f1_score :",f1_score(true_label, predicted,average='macro'))
print("precision_score :",precision_score(true_label, predicted,average='macro'))
print("recall_score :",recall_score(true_label, predicted,average='macro'))


