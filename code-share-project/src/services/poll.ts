import api from './api';

// 投票接口返回类型
export interface Poll {
  id: number;
  title: string;
  voteType: number; // 0-单选，1-多选
  userId: number;
  endTime: string;
  createTime: string;
  updateTime: string;
  options: PollOption[];
  totalVotes: number;
  isExpired: boolean;
  hasVoted?: boolean;
}

export interface PollOption {
  id: number;
  pollId: number;
  optionText: string;
  voteCount: number;
  hasVoted?: boolean;
}

export interface PollCreateParams {
  title: string;
  voteType: string | number;
  endTime: string;
  optionTextList: string[];
}

// 获取投票列表
export function getPollList(params?: any) {
  return api.post('/votes/page', {
    current: params?.page || 1,
    size: params?.size || 10,
    keyword: params?.keyword || ''
  });
}

// 获取投票详情
export function getPollDetail(id: number | string) {
  return api.post(`/votes/getById/${id}`);
}

// 创建投票
export function createPoll(data: PollCreateParams) {
  return api.post('/votes/create', data);
}

// 投票
export function vote(pollId: number | string, optionIds: number[] | string[]) {
  return api.post(`/votes/vote`, {
    voteId: pollId,
    optionIdList: optionIds
  });
}

// 删除投票
export function deletePoll(id: number | string) {
  return api.del(`/votes/delete/${id}`);
}

export default {
  getPollList,
  getPollDetail,
  createPoll,
  vote,
  deletePoll
}; 