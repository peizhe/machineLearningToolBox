#!/usr/bin/env python
#-*-encoding:utf-8-*-

__author__ = 'zxsted'

import numpy as np


def loadDataSet(filename):

    dataMat = []

    fr = open(filename,"w")

    for line in fr.readlines():
        curLine = line.strip().split("\t")
        fltLine = map(float,curLine)
        dataMat.append(fltLine)

    return dataMat



#根据指定特征 特征值 来平分数据
def binSplitDataSet(dataSet,feature,value):

    mat0 = dataSet[nonzero(dataSet[:,feature] > value)[0],:]
    mat1 = dataSet[nonzero(dataSet[:,feature] <= value)[0],:]

    return mat0,mat1

#选择最佳分裂特征
# 遍历每个特征：
#   遍历每个特征值：
#       将数据划分为两半
#       计算此时的切分误差
#       如果切分误差小于当前最小误差，更新最小误差值，当前切分为最佳切分
# 返回最佳切分的特征值和阈值
def chooseBestSplit(dataSet,leafType=regLeaf,errType=regErr,ops=(1,4)):

    tolS = ops[0]
    tolN = ops[1]

    # 如果所有的 target arible 具有相同的值： 那么退出返回值
    if len(set(dataSet[:,-1].T.tolist()[0])) == 1:  # exit cond 1
        return None,leafType(dataSet)

    m,n = np.shape(dataSet)
    #
    S = errType(dataSet)

    bestS = np.inf
    bestIndex = 0
    bestValue=0

    for featIndex in range(n-1):
        for splitVal in set(dataSet[:,featIndex]):
            mat0,mat1 = binSplitDataSet(dataSet,featIndex,splitVal)
            if (np.shape(mat0)[0] < tolN) or (np.shape(mat1)[0] < tolN):
                continue
            newS = errType(mat0) + errType(mat1)

            if newS < bestS:
                bestIndex = featIndex
                bestValue = splitVal
                bestS = newS

    # 如果减少量（S - bestS） 小于阈值， 那么不继续划分 # cond2
    if (S - bestS) < tolS:
        return None,leafType(dataSet)

    mat0,mat1 = binSplitDataSet(dataSet,bestIndex,bestValue)
    # cond 3
    if (np.shape(mat0)[0] < tolN) or (np.shape(mat1)[0] < tolN):
        return None,leafType(dataSet)

    return bestIndex,bestValue




# 计算子数据集的最后一个值（label值） 的均方和 var 是求方差已经除了个数
def regErr(dataSet):
    return np.var(dataSet[:,-1]) * np.shape(dataSet)[0]


# 返回叶子节点的值
def regLeaf(dataSet):
    return mean(dataSet[:,-1])

def getMean(tree):
    if isTree(tree['right']):tree['right'] = getMean(tree['right'])
    if isTree(tree['left']):tree['left'] = getMean(tree['left'])
    return (tree['left'] + tree['right']) / 2.0

# 修剪
def prune(tree,testData):
    if np.shape(testData)[0] == 0:
        return getMean(tree)
    #if the branches are not trees try to prune them
    if (isTree(tree['right']) or isTree(tree['left'])):
        lSet,rSet = binSplitDataSet(testData,tree['spInd'],tree['spVal'])

    if isTree(tree['left']):
        tree['left'] = prune(tree['left'],lSet)
    if isTree(tree['right']):
        tree['right'] = prune(tree['right'],rSet)
    # 如果左右子树都是叶子， 那么尝试是否可以合并他们
    if not isTree(tree['left']) and not isTree(tree['right']):
        lSet,rSet = binSplitDataSet(testData,tree['spInd'],tree['spVal'])
        errorNoMerge = np.sum(np.power(lSet[:-1] - tree['left'],2)) + \
            np.sum(np.power(rSet[:,-1] - tree['right'],2))
        treeMean = (tree['left'] + tree['right']) / 2.0

        errorMerge = np.sum(np.power(testData[:,-1] - treeMean,2))

        if errorMerge < errorNoMerge:
            print 'merging'
            return treeMean
        else:
            return tree
    else:
        return tree