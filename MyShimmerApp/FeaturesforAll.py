# window size = 1 sec, 20 samples within 1 sec ==> sampling rate = 20 Hz, Nyquist rate = 10 Hz, Wn = 0.1 (0.1*10Hz=1Hz)

from scipy.signal import filtfilt, butter
from matplotlib.pyplot import plot, legend, show, hold, grid, figure, savefig
from collections import defaultdict

import numpy as np
import scipy as sp
import math
import csv
import xlrd
import os


window_size = 2 # window_size = 1 sec
filter_order = 2

b_lowpass, a_lowpass = butter(filter_order, 0.1)
b_bandpass, a_bandpass = butter(filter_order, [0.01, 0.9], 'bandpass')

b_bandenergy, a_bandenergy = butter(filter_order, [0.03, 0.35], 'bandpass')
b_lowenergy, a_lowenergy = butter(filter_order, 0.07)
b_modvigenergy, a_modvigenergy = butter(filter_order, [0.071, 0.9], 'bandpass')

def getpass(data):
    #print 'data:', data
    lowpass = filtfilt(b_lowpass, a_lowpass, data)
    bandpass = filtfilt(b_bandpass, a_bandpass, data)
    bandenergy = filtfilt(b_bandenergy, a_bandenergy, data)
    lowenergy = filtfilt(b_lowenergy, a_lowenergy, data)
    modvigenergy = filtfilt(b_modvigenergy, a_modvigenergy, data)
    return [lowpass, bandpass, bandenergy, lowenergy, modvigenergy]
    #return [lowpass, bandpass]

def getMean(data):
    result = np.mean(data)
    return result

def getTotalMean(datax, datay, dataz):
    data = datax + datay + dataz
    result = np.mean(data)
    return result
    
def getArea(data):
    result = np.sum(data)
    return result 

def getPostureDist(datax, datay, dataz):    # differences between mean values
    diffxy = datax - datay
    diffxz = datax - dataz
    diffyz = datay - dataz
    return [diffxy, diffxz, diffyz]

def getAbsMean(data):
    result = np.mean(np.abs(data))
    return result

def getAbsArea(data):
    result = np.sum(np.abs(data))
    return result

def getTotalAbsArea(datax, datay, dataz):
    data = np.abs(datax) + np.abs(datay) + np.abs(dataz)
    result = np.sum(data)
    return result

def getTotalSVM(datax, datay, dataz):
    result = np.mean(np.sqrt(datax**2 + datay**2 + dataz**2))
    return result

#calculate the entropy of fft values
def getEntropy(data):
    data_fft = sp.fft(data)
    data_fft_abs = np.abs(data_fft)
    data_fft_abs_sum = np.sum(data_fft_abs)
    data_fft_abs_norm = data_fft_abs/data_fft_abs_sum
    data_fft_abs_norm_log2 = np.log2(data_fft_abs_norm)
    result = - np.sum(data_fft_abs_norm * data_fft_abs_norm_log2)
    result = result/len(data_fft)
    return result

def getSkew(data):
    result = sp.stats.skew(data)
    return result

def getKur(data):
    result = sp.stats.kurtosis(data)
    return result

def getQuartiles(data):
    q1 = np.percentile(np.abs(data), 25)
    q2 = np.percentile(np.abs(data), 50)
    q3 = np.percentile(np.abs(data), 75)
    return [q1, q2, q3]

#calculate the variance of data in each window
def getVar(data):
    result = np.var(data)
    return result

def getAbsCV(data):
    std = np.std(np.abs(data))
    mean = np.mean(np.abs(data))
    result = std / mean * 100.0
    return result

def getIQR(Q3, Q1):
    result = Q3 - Q1
    return result

def getRange(data):
    big = max(data)
    small = min(data)
    result = big - small
    return result

def getFFTCoeff(data):
    data_fft = sp.fft(data)
    return data_fft[1:]

def getEnergy(data):
    #print 'data:', data
    data_fft = sp.fft(data)
    #print 'data_fft:', data_fft
    data_fft_half = data_fft[1:(len(data_fft)/2+1)]
    data_fft_half_abs = np.abs(data_fft_half)
    result = np.sum(data_fft_half_abs**2)
    result = result/len(data_fft_half)
    return result
    
# calculate the second peak of autocorrelation of fft values
def getPitch(data):
    data_fft = sp.fft(data)
    result = np.correlate(data_fft, data_fft, 'full')
    result = np.sort(np.abs(result))
    return result[len(result)-2]

def getDomFreqRatio(data):
    data_fft = sp.fft(data)
    data_fft_sort = list(np.sort(abs(data_fft[:(len(data_fft)/2+1)])))
    large = data_fft_sort.pop()
    ratio = large / np.sum(data_fft_sort)
    return ratio

def getMCR(data):
    mean = np.mean(data)
    k = 0
    for i in range(len(data)-1):
        if (data[i] - mean) * (data[i+1] - mean) < 0:
            k += 1
    result = k * 1.0 / (len(data) - 1)
    return result

# Pearson's correlation coefficients
def getCorr(datax, datay, dataz):
    result0 = np.corrcoef(datax, datay)
    result1 = np.corrcoef(datax, dataz)
    result2 = np.corrcoef(datay, dataz)
    return [result0[0][1], result1[0][1], result2[0][1]]

def featureCalculation(data, Num_Col):
    line  = list()
    for i in range(Num_Col):
        #print 'Num_Col:',Num_Col,'i:',i
        [data_low[i], data_band[i], data_bandenergy[i], data_lowenergy[i], data_modvigenergy[i]] = getpass(data[i])
    for i in range(Num_Col):
        DCMean[i] = getMean(data_low[i])
        #print i, 'DCMean:', DCMean[i]
        DCArea[i] = getArea(data_low[i])
        #print i, 'DCArea:', DCArea[i]
        ACAbsMean[i] = getAbsMean(data_band[i])
        #print i, 'ACAbsMean:', ACAbsMean[i]
        ACAbsArea[i] = getAbsArea(data_band[i])
        #print i, 'ACAbsArea:', ACAbsArea[i]
        ACEntropy[i] = getEntropy(data_band[i])
        #print i, 'ACEntropy:', ACEntropy[i]
        ACSkew[i] = getSkew(data_band[i])
        #print i, 'ACSkew:', ACSkew[i]
        ACKur[i] = getKur(data_band[i])
        #print i, 'ACKur:', ACKur[i]
        ACQuartiles[i] = getQuartiles(data_band[i])
        #print i, 'ACQuartiles:', ACQuartiles[i]
        ACVar[i] = getVar(data_band[i])
        #print i, 'ACVar:', ACVar[i]
        ACAbsCV[i] = getAbsCV(data_band[i])
        #print i, 'ACAbsCV:', ACAbsCV[i]
        ACIQR[i] = getIQR(ACQuartiles[i][2], ACQuartiles[i][0])
        #print i, 'ACIQR:', ACIQR[i]
        ACRange[i] = getRange(data_band[i])
        #print i, 'ACRange:', ACRange[i]
#       ACFFTCoeff[i] = getFFTCoeff(data_band[i])
        #print i, 'ACFFTCoeff:', ACFFTCoeff[i], len(ACFFTCoeff[i])
        #if i == 0:
        ACEnergy[i] = getEnergy(data[i])    # original data
        #print i, 'ACEnergy(original):', ACEnergy[i]#, len(ACFFTCoeff[i])
        #print data_bandenergy[i]
        ACBandEnergy[i] = getEnergy(data_bandenergy[i])
        #print i, 'ACEnergy(bandenergy):', ACBandEnergy[i]
        ACLowEnergy[i] = getEnergy(data_lowenergy[i])
        #print i, 'ACEnergy(lowenergy):', ACLowEnergy[i]
        ACModVigEnergy[i] = getEnergy(data_modvigenergy[i])
        #print i, 'ACEnergy(modvigenergy):', ACModVigEnergy[i]
        ACPitch[i] = getPitch(data_band[i])
        #print i, 'ACPitch:', ACPitch[i]
        ACDomFreqRatio[i] = getDomFreqRatio(data_band[i])
        #print i, 'ACDomFreqRatio:', ACDomFreqRatio[i]
        ACMCR[i] = getMCR(data_band[i])
        #print i, 'ACMCR:', ACMCR[i]

        line = line + [DCMean[i], DCArea[i], ACAbsMean[i], ACAbsArea[i], ACEntropy[i], ACSkew[i], ACKur[i], ACQuartiles[i][0], ACQuartiles[i][1], ACQuartiles[i][2], ACVar[i], ACAbsCV[i], ACIQR[i], ACRange[i], ACEnergy[i], ACBandEnergy[i], ACLowEnergy[i], ACModVigEnergy[i], ACPitch[i], ACDomFreqRatio[i], ACMCR[i]]      # no ACFFTCoeff[i]
    
    for i in range(Num_Col/3):
        #print 'i: ', i, 'Num_Col/3',Num_Col
        DCTotalMean[i] = getTotalMean(DCMean[3*i], DCMean[3*i+1], DCMean[3*i+2])
        #print i, 'DCTotalMean:', DCTotalMean[i]
        DCPostureDist[i] = getPostureDist(DCMean[3*i], DCMean[3*i+1], DCMean[3*i+2])
        #print i, 'DCPostureDist:', DCPostureDist[i]
        ACTotalAbsArea[i] = getTotalAbsArea(data_band[3*i], data_band[3*i+1], data_band[3*i+2])
        #print i, 'ACTotalAbsArea:', ACTotalAbsArea[i]
        ACTotalSVM[i] = getTotalSVM(data_band[3*i], data_band[3*i+1], data_band[3*i+2])
        #print i, 'ACTotalSVM:', ACTotalSVM[i]
        #print [DCTotalMean[i], DCPostureDist[i][0], DCPostureDist[i][1], DCPostureDist[i][2], ACTotalAbsArea[i], ACTotalSVM[i]]        
        #   ACCorr[i] = getCorr(data_band[3*i], data_band[3*i+1], data_band[3*i+2])
        #   print i, 'ACCorr:', ACCorr[i]
        #   ACCorr[i] = getCorr(data[3*i], data[3*i+1], data[3*i+2])
        #   print i, 'ACCorr:', ACCorr[i]

        line = line + [DCTotalMean[i], DCPostureDist[i][0], DCPostureDist[i][1], DCPostureDist[i][2], ACTotalAbsArea[i], ACTotalSVM[i]]

    return line





#initial define
tmp_data = defaultdict(list)
data = defaultdict(list)
data_low = defaultdict(list)
data_band = defaultdict(list)
data_original = defaultdict(list)
data_bandenergy = defaultdict(list)
data_lowenergy = defaultdict(list)
data_modvigenergy = defaultdict(list)

DCMean = defaultdict(list)
DCArea = defaultdict(list)
DCTotalMean = defaultdict(list)
DCPostureDist = defaultdict(list)

ACAbsMean = defaultdict(list)
ACAbsArea = defaultdict(list)
ACTotalAbsArea = defaultdict(list)
ACTotalSVM = defaultdict(list)
ACEntropy = defaultdict(list)
ACSkew = defaultdict(list)
ACKur = defaultdict(list)
ACQuartiles = defaultdict(list)
ACVar = defaultdict(list)
ACAbsCV = defaultdict(list)
ACIQR = defaultdict(list)
ACRange = defaultdict(list)
ACFFTCoeff = defaultdict(list)
ACEnergy = defaultdict(list)
ACBandEnergy = defaultdict(list)
ACLowEnergy = defaultdict(list)
ACModVigEnergy = defaultdict(list)
ACPitch = defaultdict(list)
ACDomFreqRatio = defaultdict(list)
ACMCR = defaultdict(list)
ACCorr = defaultdict(list)


line = list()

count = 0
index_start = 0
index_end = 0

gap = 50
Num_Col = 6
block = [0]

def processAllFiles():
    curDir = os.getcwd()
    allFiles = os.listdir(curDir)
    InputFile = []
    for file in allFiles:
        if ".xlsx" in file:
            InputFile.append(file)

    for eachFile in InputFile:
        processOneFile(eachFile)
        
def processOneFile(readFile):
    block = [0]
    Out = readFile[0:(len(readFile)-4)]+ 'csv'
    In = readFile
    writefile = file(Out, 'wb')
    write = csv.writer(writefile)
    wb = xlrd.open_workbook(In)
    sh = wb.sheet_by_index(0)


                    
    write.writerows( [( 'DCMean_x', 'DCArea_x', 'ACAbsMean_x', 'ACAbsArea_x', 'ACEntropy_x', \
                    'ACSkew_x', 'ACKur_x', 'ACQuartiles_x1', 'ACQuartiles_x2', 'ACQuartiles_x3', \
                    'ACVar_x', 'ACAbsCV_x', 'ACIQR_x', 'ACRange_x', 'ACEnergy_x', \
                    'ACBandEnergy_x', 'ACLowEnergy_x', 'ACModVigEnergy_x', 'ACPitch_x', \
                    'ACDomFreqRatio_x', 'ACMCR_x', 'DCMean_y', 'DCArea_y', 'ACAbsMean_y', 'ACAbsArea_y', \
                    'ACEntropy_y','ACSkew_y', 'ACKur_y', 'ACQuartiles_y1', 'ACQuartiles_y2', 'ACQuartiles_y3', \
                    'ACVar_y', 'ACAbsCV_y', 'ACIQR_y', 'ACRange_y', 'ACEnergy_y', \
                    'ACBandEnergy_y', 'ACLowEnergy_y', 'ACModVigEnergy_y', 'ACPitch_y', \
                    'ACDomFreqRatio_y', 'ACMCR_y', 'DCMean_z', 'DCArea_z', 'ACAbsMean_z', 'ACAbsArea_z',\
                    'ACEntropy_z', 'ACSkew_z', 'ACKur_z', 'ACQuartiles_z1', 'ACQuartiles_z2', 'ACQuartiles_z3', \
                    'ACVar_z', 'ACAbsCV_z', 'ACIQR_z', 'ACRange_z', 'ACEnergy_z', \
                    'ACBandEnergy_z', 'ACLowEnergy_z', 'ACModVigEnergy_z', 'ACPitch_z', \
                    'ACDomFreqRatio_z', 'ACMCR_z', 'GyDCMean_x', 'GyDCArea_x', 'GyAbsMean_x', 'GyAbsArea_x', 'GyEntropy_x', \
                    'GySkew_x', 'GyKur_x', 'GyQuartiles_x1', 'GyQuartiles_x2', 'GyQuartiles_x3', \
                    'GyVar_x', 'GyAbsCV_x', 'GyIQR_x', 'GyRange_x', 'GyEnergy_x', \
                    'GyBandEnergy_x', 'GyLowEnergy_x', 'GyModVigEnergy_x', 'GyPitch_x', \
                    'GyDomFreqRatio_x', 'GyMCR_x', 'GyDCMean_y', 'GyDCArea_y', 'GyAbsMean_y', 'GyAbsArea_y', \
                    'GyEntropy_y','GySkew_y', 'GyKur_y', 'GyQuartiles_y1', 'GyQuartiles_y2', 'GyQuartiles_y3', \
                    'GyVar_y', 'GyAbsCV_y', 'GyIQR_y', 'GyRange_y', 'GyEnergy_y', \
                    'GyBandEnergy_y', 'GyLowEnergy_y', 'GyModVigEnergy_y', 'GyPitch_y', \
                    'GyDomFreqRatio_y', 'GyMCR_y', 'GyDCMean_z', 'GyDCArea_z', 'GyAbsMean_z', 'GyAbsArea_z',\
                    'GyEntropy_z', 'GySkew_z', 'GyKur_z', 'GyQuartiles_z1', 'GyQuartiles_z2', 'GyQuartiles_z3', \
                    'GyVar_z', 'GyAbsCV_z', 'GyIQR_z', 'GyRange_z', 'GyEnergy_z', \
                    'GyBandEnergy_z', 'GyLowEnergy_z', 'GyModVigEnergy_z', 'GyPitch_z', \
                    'GyDomFreqRatio_z', 'GyMCR_z', 'DCTotalMean', 'DCPostureDist_0', \
                    'DCPostureDist_1', 'DCPostureDist_2', 'ACTotalAbsArea', 'ACTotalSVM', 'GyTotalMean', 'GyPostureDist_0', \
                    'GyDCPostureDist_1', 'GyDCPostureDist_2', 'GyTotalAbsArea', 'GyTotalSVM')])

    for i in range(sh.ncols):
        data[i] = sh.col_values(i)
        del data[i][0]

    for i in range(len(data)):
        for j in range(len(data[i])):
            data[i][j] = float(data[i][j])

    
    for j in range(len(data[0]) - 1):
        #print 'data[0][j+1] ,data[0][j]', data[0][j+1], data[0][j]
        if(int(data[0][j+1]) - int(data[0][j]) > gap):
            #print 'data[0][j+1]:', data[0][j+1],'data[0][j]:', data[0][j],'j+1:',j+1 
            block.append(j+1)
    block.append(len(data[0]) - 1)

    #print 'block:',block
    for j in range(0,len(block)-1):
        #print 'j:',j,'len(block)-1:',len(block)-1
        for i in range(1, Num_Col+1):
            tmp_data[i-1] = data[i][block[j]:(block[j+1])]

        #print tmp_data, Num_Col
        result = featureCalculation(tmp_data, sh.ncols-1)
        write.writerow(result)
        tmp_data.clear()
    writefile.close()
    data.clear()
    #tmp_data.clear()
processAllFiles()
print "Done!"
        
